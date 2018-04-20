//
//  BluetoothStatusEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "EventPacket.h"

typedef NS_ENUM(uint8_t, StatusType) {
    UNKNOWN_STATUS = 0,
    BLUETOOTH_OFF = 1,
    BLUETOOTH_ERROR = 2,
    BLUETOOTH_ON = 3,
    DISCOVERY_STARTED = 4,
    DISCOVERY_ERROR = 5,
    DISCOVERY_FINISHED = 6,
};

@interface BluetoothStatusEventPacket : EventPacket
@property (nonatomic, assign) StatusType statusType;
@end
