//
//  AppDelegate.m
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import "AppDelegate.h"

NSString* HudConnectedNotificationKey = @"hudConnected";
NSString* HudConnectionFailedNotificationKey = @"hudConnectionFailed";
NSString* HudDisconnectedNotificationKey = @"hudDisconnected";
NSString* HudPeripheralExtraKey = @"hudPeripheralExtra";
NSString* HudErrorExtraKey = @"hudErrorExtra";

@interface AppDelegate () <BLEConnectionDelegate, PacketHandleDelegate>
@property (nonatomic, retain) NSTimer* heartBeatTimer;
@end

@implementation AppDelegate
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    HudPacketFilter* packetFilter = [[HudPacketFilter alloc] init];
    int iosVersion = [[[UIDevice currentDevice] systemVersion] intValue];
    
    _networkManager = [[HudNetworkManager alloc] init];
    _networkManager.iosVersion = iosVersion;
    _networkManager.connectionDelegate = self;
    
    [packetFilter addFilter:[UartConnectionEventPacket class]];
    [_networkManager registerPacketHandleDelegate:self filter:packetFilter];
    //if you want to debug packet, activate below line.
    //[_networkManager setDumpPacket:YES dumpReceivePacket:YES];
    
    NSUserDefaults* userDefaults = [NSUserDefaults standardUserDefaults];
    
    _bleAddress = [userDefaults objectForKey:[AppDelegate bleAddressPreferenceKey]];
    if(_bleAddress != nil) {
        [_networkManager connectViaUUID:_bleAddress];
    }
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}


- (void)applicationWillTerminate:(UIApplication *)application {
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
    if(_heartBeatTimer != nil) {
        [_heartBeatTimer invalidate];
        _heartBeatTimer = nil;
    }
    
    [_networkManager disconnect];
    _networkManager = nil;
}

-(void) heartBeatTimeout:(NSTimer*) timer {
    NSLog(@"Expired heart beat packet");
    [_networkManager disconnect];
}

-(void) didConnectPeripheral:(nonnull CBPeripheral*) peripheral {
    NSLog(@"Connected to ble : %@", peripheral.name);
    
    UartConnectionCheckCommandPacket* sendPacket = [[UartConnectionCheckCommandPacket alloc] init];
    [_networkManager sendPacket:sendPacket];    
    
    NSDictionary* userInfo = @{HudPeripheralExtraKey: peripheral};
    [[NSNotificationCenter defaultCenter] postNotificationName:HudConnectedNotificationKey object:nil userInfo:userInfo];
}

-(void) didFailToConnectPeripheral:(nonnull CBPeripheral*) peripheral error:(nonnull NSError *)error {
    NSLog(@"Cannot connect to ble : %@", peripheral.name);
    
    if(_heartBeatTimer != nil) {
        [_heartBeatTimer invalidate];
        _heartBeatTimer = nil;
    }
    
    NSDictionary* userInfo = @{HudPeripheralExtraKey: peripheral, HudErrorExtraKey: error};
    [[NSNotificationCenter defaultCenter] postNotificationName:HudConnectionFailedNotificationKey object:nil userInfo:userInfo];
    
    if(_bleAddress != nil) {
        [_networkManager connectViaUUID:_bleAddress];
    }
}

-(void) didDisconnectPeripheral:(nonnull CBPeripheral *)peripheral {
    NSLog(@"Disconnected from ble : %@", peripheral.name);
    
    if(_heartBeatTimer != nil) {
        [_heartBeatTimer invalidate];
        _heartBeatTimer = nil;
    }
    
    NSDictionary* userInfo = @{HudPeripheralExtraKey: peripheral};
    [[NSNotificationCenter defaultCenter] postNotificationName:HudDisconnectedNotificationKey object:nil userInfo:userInfo];
    if(_bleAddress != nil) {
        [_networkManager connectViaUUID:_bleAddress];
    }
}

-(void) onPacketReceived:(nonnull id) receivePacket {
    if([receivePacket isKindOfClass:[UartConnectionEventPacket class]]) {
        if(_heartBeatTimer != nil) {
            [_heartBeatTimer invalidate];
            _heartBeatTimer = nil;
        }
        
        _heartBeatTimer = [NSTimer scheduledTimerWithTimeInterval:10 target:self selector:@selector(heartBeatTimeout:) userInfo:nil repeats:NO];
        
        KeepAliveCommandPacket* sendPacket = [[KeepAliveCommandPacket alloc] init];
        [_networkManager sendPacket:sendPacket];
    }
}

-(void)setBleAddress:(NSString *)bleAddress {
    if(![bleAddress isEqualToString:_bleAddress]) {
        NSUserDefaults* userDefaults = [NSUserDefaults standardUserDefaults];
        
        if(bleAddress != nil) {
            [userDefaults setObject:bleAddress forKey:[AppDelegate bleAddressPreferenceKey]];
        }
        else {
            [userDefaults removeObjectForKey:[AppDelegate bleAddressPreferenceKey]];
        }
        [userDefaults synchronize];
        _bleAddress = bleAddress;
    }
}

+(NSString*) bleAddressPreferenceKey {
    return @"ble_address_pref";
}
@end
