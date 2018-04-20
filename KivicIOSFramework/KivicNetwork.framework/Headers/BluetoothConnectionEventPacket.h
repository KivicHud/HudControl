//
//  BluetoothConnectionEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "EventPacket.h"
#import "BluetoothSoundDevice.h"

typedef NS_ENUM(uint8_t, EventType){
    UNKNOWN_ERROR = 0,
    PAIRING_STARTED = 1,
    PAIRING_ERROR = 2,
    PAIRING_FINISHED = 3,
    CONNECTING = 4,
    CONNECTING_ERROR = 5,
    CONNECTED = 6,
    DISCONNECTING = 7,
    DISCONNECTING_ERROR = 8,
    DISCONNECTED = 9,
};

@interface BluetoothConnectionEventPacket : EventPacket
@property (nonatomic, nonnull, retain) BluetoothSoundDevice* soundDevice;
@property (nonatomic, assign) EventType event;
@end
