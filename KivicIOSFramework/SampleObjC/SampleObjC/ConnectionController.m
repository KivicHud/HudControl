//
//  ConnectionController.m
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import "AppDelegate.h"
#import "ConnectionController.h"
#import "BleListViewController.h"

@interface ConnectionController () <PacketHandleDelegate>
@property (nonatomic, retain) AppDelegate* appDelegate;
@property (nullable, nonatomic, copy) NSString* targetBleAddress;
@end

@implementation ConnectionController

- (void)viewDidLoad {
    [super viewDidLoad];
    _appDelegate = (AppDelegate*)[UIApplication sharedApplication].delegate;
    [_hudSwitch setOn:_appDelegate.networkManager.isConnected];
    // Do any additional setup after loading the view.
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)viewWillAppear:(BOOL)animated {
    HudPacketFilter* packetFilter = [[HudPacketFilter alloc] init];
    
    [packetFilter addFilter:[HudVersionEventPacket class]];
    
    [_appDelegate.networkManager registerPacketHandleDelegate:self filter:packetFilter];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudConnected:) name:HudConnectedNotificationKey object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudConnectionFailed:) name:HudConnectionFailedNotificationKey object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(hudDisconnected:) name:HudDisconnectedNotificationKey object:nil];
}

-(void)viewWillDisappear:(BOOL)animated {
    [_appDelegate.networkManager unregisterPacketHandleDelegate:self];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void)viewDidAppear:(BOOL)animated {
    if(_targetBleAddress != nil) {
        [_appDelegate.networkManager connectViaUUID:_targetBleAddress];
    }
    else {
        [_hudSwitch setOn:_appDelegate.networkManager.isConnected animated:YES];
    }
}

-(BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender {
    if([_hudSwitch isEqual:sender]) {
        if(!_hudSwitch.isOn) {
            return NO;
        }
    }
    return [super shouldPerformSegueWithIdentifier:identifier sender:sender];;
}

- (IBAction) selectedBLE:(UIStoryboardSegue *)segue {
    BleListViewController* bleListViewController = segue.sourceViewController;
    
    _targetBleAddress = bleListViewController.selectedBleAddress;
}

-(void) hudConnected:(NSNotification*)notification {
    CBPeripheral* peripheral = notification.userInfo[HudPeripheralExtraKey];
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:@"HUD connected." preferredStyle:UIAlertControllerStyleAlert];
    
    [_appDelegate setBleAddress:peripheral.identifier.UUIDString];
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:^{
            [self.hudSwitch setOn:YES animated:YES];
        }];
    });

}

-(void) hudConnectionFailed:(NSNotification*)notification {
    NSError* error = notification.userInfo[HudErrorExtraKey];
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:@"HUD connection fail." message:[error localizedFailureReason] preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:^{
            [self.hudSwitch setOn:NO animated:YES];
        }];
    });
}

-(void) hudDisconnected:(NSNotification*)notification {
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:@"Lost HUD connection." preferredStyle:UIAlertControllerStyleAlert];
    
    [self presentViewController:alertController animated:YES completion:nil];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        [alertController dismissViewControllerAnimated:YES completion:^{
            [self.hudSwitch setOn:NO animated:YES];
        }];
    });
}

-(void) onPacketReceived:(nonnull id) receivePacket {
    NSLog(@"Got packet : %@", receivePacket);
    
    if([receivePacket isKindOfClass:[HudVersionEventPacket class]]) {
        HudVersionEventPacket* packet = receivePacket;
        
        NSLog(@"Hud model[%@] System version[%@] BLE version[%@]", packet.modelName, packet.systemVersion, packet.bleVersion);
    }
}

- (IBAction)onHudClicked:(UISwitch *)sender {
    if(!sender.isOn) {
        HudDisconnectCommandPacket* sendPacket = [[HudDisconnectCommandPacket alloc] init];
        
        [_appDelegate.networkManager sendPacket:sendPacket];
        [_appDelegate setBleAddress:nil];
        [_appDelegate.networkManager disconnect];
    }
    
    _targetBleAddress = nil;
}
@end
