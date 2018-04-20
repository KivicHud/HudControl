//
//  DisplaySpeedGaugeCommandPacket.h
//  HudControl
//
//  Created by scpark on 2017. 11. 29..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface DisplaySpeedGaugeCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL isSpeedGaugeVisible;
@end
