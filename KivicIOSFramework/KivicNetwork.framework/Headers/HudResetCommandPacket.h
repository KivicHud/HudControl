//
//  HudResetCommandPacket.h
//  HudControl
//
//  Created by scpark on 2017. 11. 9..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "CommandPacket.h"

typedef NS_ENUM(int32_t, ResetType){
    HUD_RESET = 0,
    OBD2_RESET = 1
};

@interface HudResetCommandPacket : CommandPacket
@property (nonatomic, assign) ResetType resetType;
@end
