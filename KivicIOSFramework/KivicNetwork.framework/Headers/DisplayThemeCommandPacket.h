//
//  DisplayThemeCommandPacket.h
//  HudControl
//
//  Created by scpark on 2017. 11. 9..
//  Copyright © 2017년 kivic. All rights reserved.
//

#import "CommandPacket.h"

typedef NS_ENUM(int32_t, ThemeType){
    EMBER_THEME = 0,
    CYAN_THEME = 1,
    PINK_THEME = 2,
    YELLOW_THEME = 3,
    GREEN_THEME = 4
};

@interface DisplayThemeCommandPacket : CommandPacket
@property (nonatomic, assign) ThemeType theme;
@end
