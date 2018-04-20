//
//  NotificationPacket.h
//  HudControl
//
//  Created by scpark on 2016. 8. 31..
//  Copyright © 2016년 kivic. All rights reserved.
//

#import "HudPacket.h"

typedef NS_ENUM(uint8_t, AncsCategory) {
    CATEGORY_ID_OTHER = 0,
    CATEGORY_ID_INCOMING_CALL = 1,
    CATEGORY_ID_MISSED_CALL = 2,
    CATEGORY_ID_VOICE_MAIL = 3,
    CATEGORY_ID_SOCIAL = 4,
    CATEGORY_ID_SHCEDULE = 5,
    CATEGORY_ID_EMAIL = 6,
    CATEGORY_ID_NEWS = 7,
    CATEGORY_ID_HEALTH_AND_FITNESS = 8,
    CATEGORY_ID_BUSINESS_AND_FINANCE = 9,
    CATEGORY_ID_LOCATION = 10,
    CATEGORY_ID_ENTERTAINMENT = 11,
    CATEGORY_ID_MUSIC = 12,
    CATEGORY_ID_OBD2 = 13,
    CATEGORY_ID_SPEED = 14,
};

@interface NotificationPacket : HudPacket
@property (nonatomic, copy) NSString* packageName;
@property (nonatomic, copy) NSString* title;
@property (nonatomic, copy) NSString* message;
@property (nonatomic, assign, readonly) AncsCategory category;
@end
