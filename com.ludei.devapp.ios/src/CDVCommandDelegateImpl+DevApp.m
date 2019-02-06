//
//  CDVCommandDelegateImpl+DevApp.m
//  HelloCordova
//
//  Created by Imanol Martin on 08/04/16.
//
//

#import <Foundation/Foundation.h>
#import "Cordova/CDVCommandDelegateImpl.h"
#import "Cordova/CDVViewController.h"


@implementation CDVCommandDelegateImpl (DevApp)

- (NSString*)pathForResource:(NSString*)resourcepath
{
    return [NSString stringWithFormat:@"%@/%@/%@", NSTemporaryDirectory(), _viewController.wwwFolderName, resourcepath];
}

@end