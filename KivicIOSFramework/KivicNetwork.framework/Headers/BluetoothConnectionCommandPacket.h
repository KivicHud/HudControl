//
//  BluetoothConnectionCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface BluetoothConnectionCommandPacket : CommandPacket
@property (nonatomic, nonnull, copy) NSString* address;
@property (nonatomic, assign) BOOL isConnectionRequired;
@end
