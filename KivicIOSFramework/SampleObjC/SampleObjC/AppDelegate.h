//
//  AppDelegate.h
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HudNetworkManager.h"

extern NSString* HudConnectedNotificationKey;
extern NSString* HudConnectionFailedNotificationKey;
extern NSString* HudDisconnectedNotificationKey;
extern NSString* HudPeripheralExtraKey;
extern NSString* HudErrorExtraKey;

@interface AppDelegate : UIResponder <UIApplicationDelegate>
@property (strong, nonatomic) UIWindow *window;
@property (strong, nullable, nonatomic) HudNetworkManager *networkManager;
@property (nullable, nonatomic, copy) NSString* bleAddress; //store last ble address if needs
@end

