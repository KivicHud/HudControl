//
//  HudNetworkManager.h
//  SampleObjC
//
//  Created by scpark on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import <CoreBluetooth/CoreBluetooth.h>
#import <Foundation/Foundation.h>
#import <KivicNetwork/KivicNetwork.h>

@protocol BLEDiscoverDelegate <NSObject>
@optional
-(void) bluetoothIsReady;
-(void) didStartDiscover;
-(void) didFinishDiscover;
-(void) didDiscoverPeripheral:(nonnull CBPeripheral*) peripheral;
@end

@protocol BLEConnectionDelegate <NSObject>
@optional
-(void) didConnectPeripheral:(nonnull CBPeripheral*) peripheral;
-(void) didFailToConnectPeripheral:(nonnull CBPeripheral*) peripheral error:(nonnull NSError *)error;
-(void) didDisconnectPeripheral:(nonnull CBPeripheral *)peripheral;
@end

@interface HudNetworkManager : HudNetwork <CBCentralManagerDelegate, CBPeripheralDelegate>
@property (nonatomic, strong, nullable) id<BLEDiscoverDelegate> discoverDelegate;
@property (nonatomic, strong, nullable) id<BLEConnectionDelegate> connectionDelegate;
@property (nonatomic, assign) NSInteger iosVersion;
-(nullable CBCentralManager*) getCentralManager;
-(nullable CBPeripheral*) getConnectedPeripheral;
-(BOOL) isBluetoothEnabled;
-(BOOL) ensureBluetoothOn;
-(nullable NSArray<CBPeripheral*>*) getBondedDevices;
-(BOOL) isBondedDevices:(nonnull CBPeripheral*) peripheral;
-(BOOL) isBondedDevicesViaUUID:(nonnull NSString*) uuidString;
-(BOOL) startScan:(int) timeout;
-(void) stopScan;
-(BOOL) isConnected;
-(void) connect:(nonnull CBPeripheral*) peripheral;
-(nullable CBPeripheral*) connectViaUUID:(nonnull NSString*) uuidString;
-(nullable CBPeripheral*) getPeripheral:(nonnull NSString*) uuidString;
-(BOOL) discoverSerivce:(nonnull CBPeripheral*) peripheral;
-(void) disconnect;
-(BOOL) sendPacketImpl:(nonnull NSData*) data;
@end

