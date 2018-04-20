//
//  CommandController.h
//  SampleObjC
//
//  Created by kjchoi on 2018. 4. 19..
//  Copyright © 2018년 kivic. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CommandController : UIViewController
@property (weak, nonatomic) IBOutlet UIButton *speedColorButton;
@property (weak, nonatomic) IBOutlet UIButton *speedUnitButton;
@property (weak, nonatomic) IBOutlet UIButton *modeButton;

- (IBAction)onBacklightClicked:(UISwitch *)sender;
- (IBAction)onSpeedColorClicked:(UIButton *)sender;
- (IBAction)onSpeedUnitClicked:(UIButton *)sender;
- (IBAction)onKeyStoneChanged:(UISlider *)sender;
- (IBAction)onModeClicked:(UIButton *)sender;
- (IBAction)onFullScreenClicked:(UISwitch *)sender;
@end
