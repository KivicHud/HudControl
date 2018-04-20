//
//  BluetoothPairingCommandPacket.h
//  HudControl
//
//  Created by scpark on 2017. 2. 7..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface BluetoothPairingCommandPacket : CommandPacket
@property (nonatomic, assign) int32_t pairingType;
@property (nonatomic, nonnull, retain) NSString* soundDeviceAddress;
@property (nonatomic, nonnull, retain) NSString* pairingKey;
@end
