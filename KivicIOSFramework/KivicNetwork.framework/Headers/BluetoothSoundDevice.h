//
//  BluetoothSoundDevice.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "HudInputStream.h"
#import "HudOutputStream.h"

@interface BluetoothSoundDevice : NSObject
@property (nonatomic, nullable, copy) NSString* name;
@property (nonatomic, nullable, copy) NSString* address;
@property (nonatomic, assign) BOOL isConnected;
-(void) serialize:(nonnull HudOutputStream*) outputStream;
-(void) deserialize:(nonnull HudInputStream*) inputStream;
@end
