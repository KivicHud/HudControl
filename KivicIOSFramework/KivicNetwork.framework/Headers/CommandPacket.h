//
//  CommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 30..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HudPacket.h"

@interface CommandPacket : HudPacket
-(uint8_t) getCommand;
@end
