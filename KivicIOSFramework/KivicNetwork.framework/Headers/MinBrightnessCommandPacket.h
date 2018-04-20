//
//  MinBrightnessCommandPacket.h
//  HudControl
//
//  Created by scpark on 2018. 4. 5..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface MinBrightnessCommandPacket : CommandPacket
@property (nonatomic, assign) int32_t brightness;
@property (nonatomic, assign) BOOL showSetting;
@end
