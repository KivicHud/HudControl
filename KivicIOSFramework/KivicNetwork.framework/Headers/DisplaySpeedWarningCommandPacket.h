//
//  DisplaySpeedWarningCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 11. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface DisplaySpeedWarningCommandPacket : CommandPacket
@property (nonatomic, assign) int32_t speed;
@end
