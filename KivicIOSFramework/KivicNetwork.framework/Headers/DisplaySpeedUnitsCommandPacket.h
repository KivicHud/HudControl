//
//  DisplaySpeedUnitsCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

typedef NS_ENUM(int32_t, SpeedUnitType) {
    KM_PER_HOUR = 0,
    MILES_PER_HOUR = 1
};

@interface DisplaySpeedUnitsCommandPacket : CommandPacket
@property (nonatomic, assign) SpeedUnitType speedUnitType;
@end
