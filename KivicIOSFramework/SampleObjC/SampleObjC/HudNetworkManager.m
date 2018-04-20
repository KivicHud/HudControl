//
//  HudNetworkManager.m
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//
#import <UIKit/UIKit.h>

#import "HudNetworkManager.h"

static NSString* const BLE_IO_SERVICE = @"6E400001-B5A3-F393-E0A9-E50E24DCCA9E";
static NSString* const BLE_TX_CHARACTERISTIC = @"6E400002-B5A3-F393-E0A9-E50E24DCCA9E";
static NSString* const BLE_RX_CHARACTERISTIC = @"6E400003-B5A3-F393-E0A9-E50E24DCCA9E";
static NSUInteger const SEND_MSG_MAX_LENGTH = 20;
static NSInteger const UnknownCharacteristicWriteType = -1;

@interface HudNetworkManager ()
@property (nonatomic, retain) CBCentralManager* cbManager;
@property (nonatomic, nullable, retain) CBPeripheral* activePeripheral;
@property (nonatomic, nullable, retain) CBCharacteristic* txCharacteristic;
@property (nonatomic, nullable, retain) CBCharacteristic* rxCharacteristic;
@property (nonatomic, nonnull, retain) NSMutableData* recvPacketBuffer;
@property (nonatomic, assign) BOOL isPacketComing;
@property (nonatomic, assign) BOOL isGotWriteError;
@property (nonatomic, assign) BOOL canWritable;
@property (nonatomic, assign) NSInteger txCharacteristicWriteType;
@property (atomic, retain) NSCondition* sendLock;
@property (nonatomic, retain) NSTimer* scanTimer;
-(void) scanTimeout:(NSTimer*) timer;
-(void) cleanupPeripheral:(BOOL) clearConnection;
-(void) runOnUIThread:(void (^)(void))block;
@end

@implementation HudNetworkManager

-(id) init {
    self = [super init];
    if(self != nil) {
        self.discoverDelegate = nil;
        self.cbManager = nil;
        self.activePeripheral = nil;
        self.txCharacteristic = nil;
        self.rxCharacteristic = nil;
        self.recvPacketBuffer = [[NSMutableData alloc] init];
        self.isPacketComing = NO;
        self.isGotWriteError = NO;
        self.canWritable = NO;
        self.txCharacteristicWriteType = UnknownCharacteristicWriteType;
        self.sendLock = [[NSCondition alloc] init];
        self.scanTimer = nil;
    }
    
    return self;
}

-(nullable CBCentralManager*) getCentralManager {
    return _cbManager;
}

-(nullable CBPeripheral*) getConnectedPeripheral {
    if([self isConnected]) {
        return _activePeripheral;
    }
    
    return nil;
}

-(BOOL) isBluetoothEnabled {
    if(_cbManager == nil || _cbManager.state != CBCentralManagerStatePoweredOn) {
        return NO;
    }
    
    return YES;
}

-(BOOL) ensureBluetoothOn {
    NSLog(@"ensure bluetooth on.");
    if(_cbManager == nil) {
        _cbManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_queue_create("kivic ble queue", DISPATCH_QUEUE_SERIAL) options:@{CBCentralManagerOptionShowPowerAlertKey:[NSNumber numberWithBool:YES], CBCentralManagerOptionRestoreIdentifierKey:@"kivic ble restore"}];
    }
    else if (_cbManager.state != CBCentralManagerStatePoweredOn) {
        _cbManager.delegate = nil;
        _cbManager = nil;
        _cbManager = [[CBCentralManager alloc] initWithDelegate:self queue:dispatch_queue_create("kivic ble queue", DISPATCH_QUEUE_SERIAL) options:@{CBCentralManagerOptionShowPowerAlertKey:[NSNumber numberWithBool:YES], CBCentralManagerOptionRestoreIdentifierKey:@"kivic ble restore"}];
    }
    else {
        [self runOnUIThread:^{
            if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(bluetoothIsReady)]){
                [_discoverDelegate bluetoothIsReady];
            }
        }];
        
        return YES;
    }
    
    return NO;
}

-(nullable NSArray<CBPeripheral*>*) getBondedDevices {
    NSArray<CBPeripheral*>* peripherals = [_cbManager retrieveConnectedPeripheralsWithServices:[NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_IO_SERVICE], nil]];
    return peripherals;
}

-(BOOL) isBondedDevices:(nonnull CBPeripheral*) peripheral {
    NSArray<CBPeripheral*>* peripherals = [_cbManager retrieveConnectedPeripheralsWithServices:[NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_IO_SERVICE], nil]];
    if(peripherals != nil && peripherals.count > 0) {
        for(CBPeripheral* p in peripherals) {
            if([p.identifier.UUIDString isEqualToString:peripheral.identifier.UUIDString]) {
                return YES;
            }
        }
    }
    
    return NO;
}

-(BOOL) isBondedDevicesViaUUID:(nonnull NSString*) uuidString {
    NSArray<CBPeripheral*>* peripherals = [_cbManager retrieveConnectedPeripheralsWithServices:[NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_IO_SERVICE], nil]];
    if(peripherals != nil && peripherals.count > 0) {
        for(CBPeripheral* p in peripherals) {
            if([p.identifier.UUIDString isEqualToString:uuidString]) {
                return YES;
            }
        }
    }
    
    return NO;
}

-(void) scanTimeout:(NSTimer*) timer {
    NSLog(@"scan timed out.");
    _scanTimer = nil;
    [_cbManager stopScan];
    [self runOnUIThread:^{
        if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(didFinishDiscover)]) {
            [_discoverDelegate didFinishDiscover];
        }
    }];
}

-(BOOL)startScan :(int) timeout {
    if(_cbManager.state == CBCentralManagerStatePoweredOn) {
        [_cbManager stopScan];
        
        if(_scanTimer != nil) {
            [_scanTimer invalidate];
            _scanTimer = nil;
        }
        
        NSLog(@"start scan.");
        _scanTimer = [NSTimer scheduledTimerWithTimeInterval:(float) timeout target:self selector:@selector(scanTimeout:) userInfo:nil repeats:NO];
        [_cbManager scanForPeripheralsWithServices:nil options:nil];
        [self runOnUIThread:^{
            if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(didStartDiscover)]) {
                [_discoverDelegate didStartDiscover];
            }
        }];
        
        return YES;
    }
    
    return NO;
}

-(void)stopScan {
    NSLog(@"stop scan.");
    if(_cbManager.isScanning) {
        [_cbManager stopScan];
        
        if(_scanTimer != nil) {
            [_scanTimer invalidate];
            _scanTimer = nil;
        }
    }
    
    [self runOnUIThread:^{
        if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(didFinishDiscover)]) {
            [_discoverDelegate didFinishDiscover];
        }
    }];
}

-(BOOL) isConnected {
    if(_activePeripheral != nil && _activePeripheral.state == CBPeripheralStateConnected) {
        return YES;
    }
    
    return NO;
}

-(void) connect:(nonnull CBPeripheral*) peripheral {
    if(_cbManager.isScanning) {
        [_cbManager stopScan];
        
        if(_scanTimer != nil) {
            [_scanTimer invalidate];
            _scanTimer = nil;
        }
    }
    
    if(peripheral == nil) {
        return;
    }
    
    if(_activePeripheral != nil) {
        switch (_activePeripheral.state) {
            case CBPeripheralStateConnected:
            case CBPeripheralStateConnecting:
                if([_activePeripheral.identifier isEqual:peripheral.identifier]) {
                    NSLog(@"already requested to connect to peripheral : %@", _activePeripheral);
                    return;
                }
                break;
            default:
                break;
        }
        
        if(_iosVersion >= 11) {
            [self cleanupPeripheral:NO];
        }
        else {
            [self cleanupPeripheral:YES];
        }
    }
    
    NSLog(@"trying to connect to peripheral : %@", peripheral);
    _activePeripheral = peripheral;
    _activePeripheral.delegate = self;
    [_cbManager connectPeripheral:_activePeripheral options:@{CBConnectPeripheralOptionNotifyOnConnectionKey: [NSNumber numberWithBool:YES]}];
}

-(nullable CBPeripheral*) connectViaUUID:(nonnull NSString*) uuidString {
    NSArray* peripherals = [_cbManager retrievePeripheralsWithIdentifiers:[NSArray arrayWithObjects:[[NSUUID alloc]initWithUUIDString:uuidString], nil]];
    if(peripherals != nil && peripherals.count > 0) {
        CBPeripheral* peripheral = [peripherals objectAtIndex:0];
        [self connect:peripheral];
        return peripheral;
    }
    
    NSLog(@"Can't connect to %@ due to not found.", uuidString);
    return nil;
}

-(nullable CBPeripheral*) getPeripheral:(nonnull NSString*) uuidString {
    NSArray* peripherals = [_cbManager retrievePeripheralsWithIdentifiers:[NSArray arrayWithObjects:[[NSUUID alloc]initWithUUIDString:uuidString], nil]];
    if(peripherals != nil && peripherals.count > 0) {
        NSLog(@"getPeripheral found peripheral");
        return [peripherals objectAtIndex:0];
    }
    else {
        peripherals = [_cbManager retrieveConnectedPeripheralsWithServices:[NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_IO_SERVICE], nil]];
        if(peripherals != nil && peripherals.count > 0) {
            for(CBPeripheral* peripheral in peripherals) {
                if([uuidString isEqualToString:peripheral.identifier.UUIDString]) {
                    return peripheral;
                }
            }
        }
    }
    NSLog(@"getPeripheral not found peripheral");
    return nil;
}

-(BOOL) discoverSerivce:(nonnull CBPeripheral*) peripheral {
    if(peripheral.state == CBPeripheralStateConnected) {
        [peripheral discoverServices:nil];
        return YES;
    }
    
    return NO;
}

-(void) disconnect {
    NSLog(@"disconnect");
    _canWritable = NO;
    [self cleanupPeripheral:YES];
}

-(BOOL) sendPacketImpl:(nonnull NSData*) data {
    @synchronized (self) {
        uint8_t const* ptrSrc = (uint8_t const*)[data bytes];
        NSUInteger packetCount = data.length/SEND_MSG_MAX_LENGTH;
        if(data.length%SEND_MSG_MAX_LENGTH > 0) {
            ++packetCount;
        }
        
        if(_activePeripheral == nil || _activePeripheral.state != CBPeripheralStateConnected) {
            NSLog(@"BLE is not connected.");
            return NO;
        }
            
        for(NSUInteger idx = 0 ; idx < packetCount ; ++idx, ptrSrc += SEND_MSG_MAX_LENGTH) {
            NSUInteger available = SEND_MSG_MAX_LENGTH;
            if(idx + 1 >= packetCount) {
                available = data.length - idx*SEND_MSG_MAX_LENGTH;
            }
            
            NSData* sendBuffer = [NSData dataWithBytes:ptrSrc length:available];            
            if(_canWritable) {
                if((_txCharacteristic.properties & CBCharacteristicPropertyWriteWithoutResponse) == CBCharacteristicPropertyWriteWithoutResponse) {
                    [_activePeripheral writeValue:sendBuffer forCharacteristic:_txCharacteristic type:_txCharacteristicWriteType];
                    if(_txCharacteristicWriteType == CBCharacteristicWriteWithoutResponse) {
                        [NSThread sleepForTimeInterval:0.02]; //sleep 20ms to avoid data loss
                    }
                }
                else if((_txCharacteristic.properties & CBCharacteristicPropertyWrite) == CBCharacteristicPropertyWrite) {
                    [_activePeripheral writeValue:sendBuffer forCharacteristic:_txCharacteristic type:_txCharacteristicWriteType];
                }
                else {
                    NSLog(@"There is no permission to write to ble.(0x%lX)", (unsigned long)_txCharacteristic.properties);
                    return NO;
                }
            }
            else {
                NSLog(@"We can't write packet to ble.");
                return NO;
            }
        }
    }
    return YES;
}

-(void)centralManager:(CBCentralManager *)central willRestoreState:(NSDictionary<NSString *,id> *)dict {
    NSArray<CBPeripheral*>* restoredStatePeripherals = dict[CBCentralManagerRestoredStatePeripheralsKey];
  
    NSLog(@"willRestoreState invoked.(_cbManager : %@ central : %@ activePeripheral : %@)", _cbManager, central, _activePeripheral);
    if(restoredStatePeripherals != nil && [restoredStatePeripherals count] > 0) {
        _activePeripheral = [restoredStatePeripherals objectAtIndex:0];
        _activePeripheral.delegate = self;
        NSLog(@"willRestoreState (restoredStatePeripheral : %@)", _activePeripheral);
    }
    else {
        _activePeripheral = nil;
    }
    
    NSLog(@"finally activePeripheral : %@ in willRestoreState", _activePeripheral);
}

-(void)centralManagerDidUpdateState:(CBCentralManager *)central {
    NSLog(@"centralManagerDidUpdateState : %ld _discoverDelegate : %@", (long)central.state, _discoverDelegate);
    if(central.state == CBCentralManagerStatePoweredOn) {
        if(_activePeripheral != nil) {
            if(_activePeripheral.state == CBPeripheralStateConnected ||
               _activePeripheral.state == CBPeripheralStateConnecting) {
                if (_activePeripheral.services != nil) {
                    NSUInteger serviceUUIDIndex = [_activePeripheral.services indexOfObjectPassingTest:^BOOL(CBService *obj, NSUInteger index, BOOL *stop) {
                        return [obj.UUID isEqual:[CBUUID UUIDWithString:BLE_IO_SERVICE]];
                    }];
                    
                    if (serviceUUIDIndex == NSNotFound) {
                        NSLog(@"Can't find our service. trying to invoke discoverServices");
                        [_activePeripheral discoverServices:nil];
                    }
                    else {
                        if (_activePeripheral.services[serviceUUIDIndex].characteristics != nil) {
                            [self peripheral:_activePeripheral didDiscoverCharacteristicsForService:_activePeripheral.services[serviceUUIDIndex] error:nil];
                        }
                        else {
                            NSLog(@"Can't find characteristics. trying to invoke discoverCharacteristics");
                            NSArray* requiredCharacteristics = [NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_TX_CHARACTERISTIC], [CBUUID UUIDWithString:BLE_RX_CHARACTERISTIC], nil];
                            [_activePeripheral discoverCharacteristics:requiredCharacteristics forService:_activePeripheral.services[serviceUUIDIndex]];
                        }
                    }
                }
                else {
                    NSLog(@"There is no services. trying to invoke discoverServices");
                    [_activePeripheral discoverServices:nil];
                }
            }
        }
        else {
            [self runOnUIThread:^{
                if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(bluetoothIsReady)]) {
                    [_discoverDelegate bluetoothIsReady];
                }
                else {
                    NSLog(@"No such delegate bluetoothIsReady");
                }
            }];
        }
    }
}

-(void)centralManager:(CBCentralManager *)central didDiscoverPeripheral:(CBPeripheral *)peripheral advertisementData:(NSDictionary<NSString *,id> *)advertisementData RSSI:(NSNumber *)RSSI {
    if([peripheral.name hasPrefix:@"KIVIC HUD"]) {
        [self runOnUIThread:^{
            if(_discoverDelegate != nil && [_discoverDelegate respondsToSelector:@selector(didDiscoverPeripheral:)]) {
                [_discoverDelegate didDiscoverPeripheral:peripheral];
            }
            else {
                NSLog(@"No such delegate didDiscoverPeripheral");
            }
        }];
    }
}

-(void)centralManager:(CBCentralManager *)central didConnectPeripheral:(CBPeripheral *)peripheral {
    NSLog(@"connected to peripheral[%@] %@.", peripheral.name, peripheral.identifier.UUIDString);
    [peripheral discoverServices:nil];
}

-(void)centralManager:(CBCentralManager *)central didFailToConnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Error didFailToConnectPeripheral: %@", [error localizedDescription]);
    if(_iosVersion >= 11) {
        [self cleanupPeripheral:NO];
    }
    else {
        [self cleanupPeripheral:YES];
    }
    [self runOnUIThread:^{
        if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(didFailToConnectPeripheral:error:)]) {
            [_connectionDelegate didFailToConnectPeripheral:peripheral error:error];
        }
        else {
            NSLog(@"No such delegate didFailToConnectPeripheral");
        }
    }];
    
}

-(void)centralManager:(CBCentralManager *)central didDisconnectPeripheral:(CBPeripheral *)peripheral error:(NSError *)error {
    NSLog(@"Error didDisconnectPeripheral: %@", [error localizedDescription]);
    if(_iosVersion >= 11) {
        [self cleanupPeripheral:NO];
    }
    else {
        [self cleanupPeripheral:YES];
    }
    [self runOnUIThread:^{
        if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(didDisconnectPeripheral:)]) {
            [_connectionDelegate didDisconnectPeripheral:peripheral];
        }
        else {
            NSLog(@"No such delegate didDisconnectPeripheral");
        }
    }];
}

-(void)peripheral:(CBPeripheral *)peripheral didDiscoverServices:(NSError *)error {
    if(error) {
        NSLog(@"Error discovering services: %@", [error localizedDescription]);
        [self runOnUIThread:^{
            if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(centralManager:didFailToConnectPeripheral:error:)]) {
                [_connectionDelegate didFailToConnectPeripheral:peripheral error:error];
            }
        }];
        
        if(_iosVersion >= 11) {
            [self cleanupPeripheral:NO];
        }
        else {
            [self cleanupPeripheral:YES];
        }
        return;
    }
    
    for (CBService* service in peripheral.services) {
        NSLog(@"found service %@", service.UUID.UUIDString);
        if([service.UUID.UUIDString isEqualToString:BLE_IO_SERVICE]) {
            NSArray* requiredCharacteristics = [NSArray arrayWithObjects:[CBUUID UUIDWithString:BLE_TX_CHARACTERISTIC], [CBUUID UUIDWithString:BLE_RX_CHARACTERISTIC], nil];
            [peripheral discoverCharacteristics:requiredCharacteristics forService:service];
            return;
        }
    }
    
    NSLog(@"not found io service");
}

-(void)peripheral:(CBPeripheral *)peripheral didDiscoverCharacteristicsForService:(CBService *)service error:(NSError *)error {
    if(error) {
        NSLog(@"Error discovering characteristics: %@", [error localizedDescription]);
        [self runOnUIThread:^{
            if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(didFailToConnectPeripheral:error:)]) {
                [_connectionDelegate didFailToConnectPeripheral:peripheral error:error];
            }
            else {
                NSLog(@"No such delegate didFailToConnectPeripheral");
            }
        }];
        
        if(_iosVersion >= 11) {
            [self cleanupPeripheral:NO];
        }
        else {
            [self cleanupPeripheral:YES];
        }
        return;
    }
    
    for (CBCharacteristic *characteristic in service.characteristics) {
        if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:BLE_TX_CHARACTERISTIC]]) {
            if((characteristic.properties & CBCharacteristicPropertyWriteWithoutResponse) == CBCharacteristicPropertyWriteWithoutResponse) {
                _txCharacteristicWriteType = CBCharacteristicWriteWithoutResponse;
            }
            else if((characteristic.properties & CBCharacteristicPropertyWrite) == CBCharacteristicPropertyWrite) {
                _txCharacteristicWriteType = CBCharacteristicWriteWithResponse;
            }
            else {
                NSLog(@"unknown  tx characteristic.properties[0x%lX]", (unsigned long)characteristic.properties);
            }
            
            if(_txCharacteristicWriteType != UnknownCharacteristicWriteType){
                _txCharacteristic = characteristic;
                [peripheral setNotifyValue:YES forCharacteristic:characteristic];
            }
        }
        else if ([characteristic.UUID isEqual:[CBUUID UUIDWithString:BLE_RX_CHARACTERISTIC]]) {
            _rxCharacteristic = characteristic;
            [peripheral setNotifyValue:YES forCharacteristic:characteristic];
        }
    }
    
    if(_txCharacteristic == nil || _rxCharacteristic == nil) {
        [self runOnUIThread:^{
            if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(didFailToConnectPeripheral:error:)]) {
                NSError* error = [NSError errorWithDomain:@"com.kivic.HudControl" code:100 userInfo:nil];
                [_connectionDelegate didFailToConnectPeripheral:peripheral error:error];
            }
            else {
                NSLog(@"No such delegate didFailToConnectPeripheral");
            }
        }];
        
        if(_iosVersion >= 11) {
            [self cleanupPeripheral:NO];
        }
        else {
            [self cleanupPeripheral:YES];
        }
    }
    else {
        _canWritable = YES;
        [self runOnUIThread:^{
            if(_connectionDelegate != nil && [_connectionDelegate respondsToSelector:@selector(didConnectPeripheral:)]) {
                [_connectionDelegate didConnectPeripheral:peripheral];
            }
            else {
                NSLog(@"No such delegate didConnectPeripheral");
            }
        }];
    }
}

-(void)peripheral:(CBPeripheral *)peripheral didUpdateValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    if(error) {
        NSLog(@"Error didUpdateValueForCharacteristic: %@", [error localizedDescription]);
    }
    else if(characteristic == _rxCharacteristic && characteristic.value.length > 0){
        uint8_t const* ptrBuffer = (uint8_t const*)[characteristic.value bytes];
        for(NSUInteger idx = 0 ; idx < characteristic.value.length ; ++idx, ++ptrBuffer) {
            if(_isPacketComing) {
                [_recvPacketBuffer appendBytes:ptrBuffer length:1];
                if(ptrBuffer[0] == ETX) {
                    @try {
                        [self handleRawPacket:_recvPacketBuffer];
                    } @catch (NSException *exception) {
                        NSLog(@"cannot parse raw packet(%@)", exception.reason);
                    }
                    
                    _isPacketComing = NO;
                }
                else if(ptrBuffer[0] == STX){
                    _isPacketComing = YES;
                    [_recvPacketBuffer setLength:0];
                    [_recvPacketBuffer appendBytes:ptrBuffer length:1];
                }
            }
            else if(ptrBuffer[0] == STX){
                _isPacketComing = YES;
                [_recvPacketBuffer setLength:0];
                [_recvPacketBuffer appendBytes:ptrBuffer length:1];
            }
            else {
                NSLog(@"unknown data : 0x%X", ptrBuffer[0]);
            }
        }
    }
}

-(void)peripheral:(CBPeripheral *)peripheral didWriteValueForCharacteristic:(CBCharacteristic *)characteristic error:(NSError *)error {
    
    if(error) {
        NSLog(@"Error didWriteValueForCharacteristic: %@", [error localizedDescription]);
        _isGotWriteError = YES;
    }
    
    NSLog(@"didWriteValueForCharacteristic succeed.");
}

-(void) cleanupPeripheral:(BOOL) clearConnection {
    NSLog(@"cleanupPeripheral clearConnection : %d", clearConnection);
    if(_activePeripheral != nil) {
        if (_activePeripheral.services != nil) {
            for (CBService* service in _activePeripheral.services) {
                if (service.characteristics != nil) {
                    for (CBCharacteristic* characteristic in service.characteristics) {
                        if (characteristic.isNotifying) {
                            [_activePeripheral setNotifyValue:NO forCharacteristic:characteristic];
                        }
                    }
                }
            }
        }
        
        if(clearConnection) {
            [_cbManager cancelPeripheralConnection:_activePeripheral];
        }
//        [_cbManager cancelPeripheralConnection:_activePeripheral];
        _txCharacteristic = nil;
        _rxCharacteristic = nil;
        _activePeripheral = nil;
        _canWritable = NO;
        _txCharacteristicWriteType = UnknownCharacteristicWriteType;
    }
}

-(void) runOnUIThread:(void (^)(void))block {
    if ([[NSThread currentThread] isMainThread]){
        block();
    }
    else {
        dispatch_sync(dispatch_get_main_queue(), block);
    }
}
@end
