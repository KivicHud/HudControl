//
//  BluetoothDeviceFoundEventPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <KivicNetwork/EventPacket.h>
#import "BluetoothSoundDevice.h"

@interface BluetoothDeviceFoundEventPacket : EventPacket
@property (nonatomic, nonnull, retain) BluetoothSoundDevice* soundDevice;
@end
