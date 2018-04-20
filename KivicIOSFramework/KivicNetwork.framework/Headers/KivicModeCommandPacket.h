//
//  KivicModeCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

typedef NS_ENUM(int32_t, KivicModeType) {
    ANDROID_MODE                    = 0,
    IOS_MODE                        = 1,
    ANDROID_KIVICCAST_MODE          = 2,
    IOS_STA_MODE                    = 3,
    ANDROID_KIVICCAST_STA_MODE      = 4,
    IOS_TBT_STA_MODE                = 5,
    ANDROID_TBT_STA_MODE            = 6,
    ANDROID_HUD_MODE                = 7,
    IOS_HUD_MODE                    = 8,
    ANDROID_TBT_MODE                = 9,
    IOS_TBT_MODE                    = 10,
    IOS_KIVICCAST_MODE              = 11,
    IOS_KIVICCAST_STA_MODE          = 12,
};


@interface KivicModeCommandPacket : CommandPacket
@property (nonatomic, assign) KivicModeType mode;
@end
