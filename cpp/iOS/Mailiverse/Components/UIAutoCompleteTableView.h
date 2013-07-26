/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#include "MMTypes.h"

@protocol UIAutoCompleteDelegate <NSObject>

-(NSArray *) valuesFor:(NSString *)key;
-(void) onSelected:(NSString *)text;

@end 

@interface UIAutoCompleteTableView : UITableView <UITableViewDataSource, UITableViewDelegate>

@property (MM_WEAK, nonatomic) id autoCompleteDelegate;
- (void) onKeyChange:(NSString *)key;

@end
