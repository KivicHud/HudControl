//
//  BluetoothPairingEventPacket.h
//  HudControl
//
//  Created by scpark on 2017. 2. 7..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "EventPacket.h"

#define BLUETOOTH_PAIRING_PIN_CODE_TYPE      0
#define BLUETOOTH_PAIRING_PASSKEY_TYPE       1

@interface BluetoothPairingEventPacket : EventPacket
@property (nonatomic, assign) int32_t pairingType;
@property (nonatomic, nonnull, retain) NSString* soundDeviceAddress;
@end
