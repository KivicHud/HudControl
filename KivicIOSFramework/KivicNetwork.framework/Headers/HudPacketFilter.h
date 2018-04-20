//
//  HudPacketFilter.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HudPacketFilter : NSObject
-(BOOL) addFilter:(Class) clazz;
-(BOOL) accept:(Class) clazz;
@end
