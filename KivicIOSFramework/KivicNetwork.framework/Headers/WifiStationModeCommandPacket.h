//
//  WifiStationModeCommandPacket.h
//  HudControl
//
//  Created by scpark on 2017. 5. 31..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "CommandPacket.h"

typedef NS_ENUM(int32_t, HotSpotSecurityType){
    HOTSPOT_SECURITY_OPEN  = 0,
    HOTSPOT_SECURITY_WPA   = 1,
    HOTSPOT_SECURITY_WPA2  = 2
};

@interface WifiStationModeCommandPacket : CommandPacket
@property (nonatomic, nonnull, retain) NSString* ssid;
@property (nonatomic, nonnull, retain) NSString* password;
@property (nonatomic, assign) HotSpotSecurityType securityType;
@end
