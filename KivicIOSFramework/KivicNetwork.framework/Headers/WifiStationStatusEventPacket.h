//
//  WifiStationStatusEventPacket.h
//  HudControl
//
//  Created by scpark on 2017. 5. 31..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "EventPacket.h"

typedef NS_ENUM(int32_t, WifiStationEventType){
    WIFI_STATION_UNKNOWN_STATE             = 0,
    WIFI_STATION_CONNECTED                 = 1,
    WIFI_STATION_DISCONNECTED              = 2,
    WIFI_STATION_REQUEST_ENABLE_HOTSPOT    = 3,
    WIFI_STATION_NO_SUCH_SSID              = 4,
    WIFI_STATION_INVALID_INFORMATION       = 5,
    WIFI_STATION_EMPTY_INFORMATION         = 6,
};

@interface WifiStationStatusEventPacket : EventPacket
@property (nonatomic, assign) WifiStationEventType eventType;
@property (nonatomic, retain) NSString* errorMessage;
@property (nonatomic, retain) NSString* ipAddress;
@end
