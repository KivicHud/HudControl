//
//  ConnectionController.h
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ConnectionController : UIViewController
@property (weak, nonatomic) IBOutlet UISwitch *hudSwitch;

- (IBAction) selectedBLE:(UIStoryboardSegue *)segue;
- (IBAction)onHudClicked:(UISwitch *)sender;
@end
