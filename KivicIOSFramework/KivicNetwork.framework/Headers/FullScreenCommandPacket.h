//
//  FullScreenCommandPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface FullScreenCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL isFullScreen;
@end