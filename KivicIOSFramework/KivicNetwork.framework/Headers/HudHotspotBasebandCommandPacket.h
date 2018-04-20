//
//  HudHotspotBasebandCommandPacket.h
//  KivicNetwork
//
//  Created by kjchoi on 2018. 4. 16..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import "CommandPacket.h"

@interface HudHotspotBasebandCommandPacket : CommandPacket
@property (nonatomic, assign) BOOL is5G;
@property (nonatomic, assign) BOOL forceEnable;
@end
