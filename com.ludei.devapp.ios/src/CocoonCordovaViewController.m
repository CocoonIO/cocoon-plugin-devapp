//
//  CocoonCordovaViewController.m
//  HelloCordova
//
//  Created by Imanol Fernandez Gorostizag on 3/3/15.
//
//

#import "CocoonCordovaViewController.h"
#import <Cordova/NSDictionary+CordovaPreferences.h>

@interface CDVUIWebViewNavigationDelegate : NSObject <UIWebViewDelegate>
@property (nonatomic, weak) CDVPlugin* enginePlugin;
- (instancetype)initWithEnginePlugin:(CDVPlugin*)enginePlugin;
@end

typedef enum CocoonEngine
{
    ENGINE_WEBVIEW = 0,
    ENGINE_WEBVIEW_PLUS = 1,
    ENGINE_CANVAS_PLUS = 2
} CocoonEngine;


@implementation CocoonCordovaViewController
{
    UIInterfaceOrientationMask devAppOrientations;
    CocoonEngine engine;
    CDVUIWebViewNavigationDelegate * wbChainDelegate;
    __weak id<WKNavigationDelegate> wkChainDelegate;
}

+ (UIViewController*) createWithURL: (NSURL*) url settings:(NSDictionary*)settings
{
    return [[CocoonCordovaViewController alloc] initWithURL:url settings:settings];
}

- (id)initWithURL: (NSURL*) url settings:(NSDictionary*)settings;
{
    self = [super init];
    if (self) {
        self.cocoonSettings = settings;
        
        [self initSupportedOrientations];
        
        engine = ENGINE_WEBVIEW_PLUS;
        
        NSNumber * n = [self.cocoonSettings objectForKey:@"launch_in_webview"];
        if (n && !n.boolValue) {
            engine = ENGINE_CANVAS_PLUS;
            
            /* Enable loadOnAppear because cordova calls loadURL in viewDidLoad and the view is not yet autorotated if the game is in landscape (causing incorrect window innerWidth/innerHeight size)
             *  Thanks to this delay canvas+ will get the correct view size when it is called from viewDidLoad and the user wants landscape autorotation
             */
            //engine instance is not created yet, use static method
            [self.cocoonSettings setValue:@1 forKeyPath:@"accelerated_webview"];
            [NSClassFromString(@"CanvasPlusEngine") performSelector:NSSelectorFromString(@"enableLoadOnDidAppear")];
            self.startPage = [url absoluteString];
        }
        else
        {
            n = [self.cocoonSettings objectForKey:@"accelerated_webview"];
            if (n && n.boolValue) {
                engine = ENGINE_WEBVIEW_PLUS;
            }
            else
            {
                // We use the internal webview if Canvas+ is available so we can support both Cordova and OpenSDK plugins
                if (NSClassFromString(@"CanvasPlusEngine") != NULL)
                {
                    [self.cocoonSettings setValue:@1 forKeyPath:@"launch_in_webview"];
                    [self.cocoonSettings setValue:@1 forKeyPath:@"accelerated_webview"];
                    engine = ENGINE_CANVAS_PLUS;
                }
                else
                {
                    engine = ENGINE_WEBVIEW_PLUS;
                }
            }
            
            // We remove Cookies, Caches and WebKit directories, otherwise WKWebview will show cached content
            
            NSFileManager *fileManager = [NSFileManager defaultManager];
            
            NSString* libraryPath = [NSHomeDirectory() stringByAppendingPathComponent:@"Library"];
            NSString* cachePath = [libraryPath stringByAppendingPathComponent:@"Caches"];
            NSError *error;
            [fileManager removeItemAtPath:[cachePath stringByAppendingPathComponent:[[NSBundle mainBundle] bundleIdentifier]]error:&error];
            
            NSArray* webviewFiles = [NSArray arrayWithObjects:@"Cookies", nil];
            for (NSString* webviewFile in webviewFiles) {
                NSString* path = [libraryPath stringByAppendingPathComponent:webviewFile];
                
                [fileManager removeItemAtPath:path error:&error];
            }
            
            if (engine == ENGINE_WEBVIEW_PLUS) {
                if (!url.isFileURL) {
                    self.startPage = [url absoluteString];
                    
                } else {
                    NSString* alternateContentSrc = [self.commandDelegate.settings cordovaSettingForKey:@"AlternateContentSrc"];
                    if (alternateContentSrc != NULL) {
                        self.startPage = [NSString stringWithFormat:@"%@/%@/%@",
                                          NSTemporaryDirectory(),
                                          self.wwwFolderName,
                                          alternateContentSrc];
                    }
                }
                
            } else {
                if (!url.isFileURL) {
                    self.startPage = [url absoluteString];
                }
            }
        }
        
        self.cocoonURL = [self prepareURL:url];
    }
    return self;
}

- (UIView*)newCordovaViewWithFrame:(CGRect)bounds {
    [self changeDefaultCordovaEngine];

    // Copy the cordova.js files (cordova.js, cordova_plugins.js and plugins)
    NSFileManager *fileManager = [NSFileManager defaultManager];
    if ([self.cocoonURL isFileURL]) {
        NSArray* cordovaFiles = [NSArray arrayWithObjects:@"cordova.js", @"cordova_plugins.js", @"plugins", nil];
        for (NSString* cordovaFile in cordovaFiles) {
            NSString* tmpPath = [NSHomeDirectory() stringByAppendingPathComponent:[@"tmp/www/" stringByAppendingString:cordovaFile]];
            
            NSBundle* mainBundle = [NSBundle mainBundle];
            NSMutableArray* directoryParts = [NSMutableArray arrayWithArray:[cordovaFile componentsSeparatedByString:@"/"]];
            NSString* filename = [directoryParts lastObject];
            
            [directoryParts removeLastObject];
            
            NSString* directoryPartsJoined = [directoryParts componentsJoinedByString:@"/"];
            NSString* directoryStr = self.wwwFolderName;
            
            if ([directoryPartsJoined length] > 0) {
                directoryStr = [NSString stringWithFormat:@"%@/%@", self.wwwFolderName, [directoryParts componentsJoinedByString:@"/"]];
            }
            
            NSString* cordovajsPath = [mainBundle pathForResource:filename ofType:@"" inDirectory:directoryStr];
            if ([fileManager fileExistsAtPath:cordovajsPath]) {
                if ([[NSFileManager defaultManager] fileExistsAtPath:tmpPath]) {
                    [[NSFileManager defaultManager] removeItemAtPath:tmpPath error:nil];
                }
                NSError *copyError = nil;
                if (![fileManager copyItemAtPath:cordovajsPath toPath:tmpPath error:&copyError]) {
                    NSLog(@"Error copying files: %@", [copyError localizedDescription]);
                }
            }
        }
    }
    
    UIView * result = [super newCordovaViewWithFrame:bounds];
    if (engine == ENGINE_WEBVIEW || [self.webViewEngine isKindOfClass:NSClassFromString(@"CDVUIWebViewEngine")]) {
        wbChainDelegate = [[CDVUIWebViewNavigationDelegate alloc] initWithEnginePlugin:self.webViewEngine];
    }
    else if (engine == ENGINE_WEBVIEW_PLUS && [self.webViewEngine conformsToProtocol:@protocol(WKNavigationDelegate)]) {
        wkChainDelegate = (id<WKNavigationDelegate>)self.webViewEngine;
    }
    
    return result;
}

-(void) viewDidLoad
{
    [super viewDidLoad];
    [self initDebug];
}

-(void) viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if ([self.webViewEngine respondsToSelector:@selector(viewDidAppear:)]) {
        [self.webViewEngine performSelector:@selector(viewDidAppear:) withObject:[NSNumber numberWithBool:animated]];
    }
}


-(void) changeDefaultCordovaEngine
{
    NSString * cordovaEngine;
    switch (engine) {
        default:
        case ENGINE_WEBVIEW: cordovaEngine = @"CDVUIWebViewEngine";
            break;
        case ENGINE_WEBVIEW_PLUS: cordovaEngine = @"CDVWKWebViewEngine";
            break;
        case ENGINE_CANVAS_PLUS: cordovaEngine = @"CanvasPlusEngine";
            break;
    }
    [self.settings setObject:cordovaEngine forKey:@"CordovaWebViewEngine".lowercaseString];
}

- (void)loadURL:(NSURL *)url
{
    _cocoonURL = [self prepareURL:url];
}


- (NSURL *) prepareURL:(NSURL * ) url
{
    if (url.isFileURL && ![url.lastPathComponent.lowercaseString hasPrefix:@".html"]) {
        return [url URLByAppendingPathComponent:@"index.html"];
    }
    return url;
}

- (NSURL*)appUrl
{
    return self.cocoonURL;
}

- (void) initSupportedOrientations
{
    typedef enum CocoonJSOrientation
    {
        ORIENTATION_LANDSCAPE = 0,
        ORIENTATION_PORTRAIT = 1,
        ORIENTATION_BOTH = 2,
        ORIENTATION_FROM_PLIST = 3,
    } CocoonJSOrientation;
    
    devAppOrientations = 0;
    
    NSNumber * n = [self.cocoonSettings objectForKey:@"orientation"];
    
    if (!n) {
        return;
    }
    
    CocoonJSOrientation orientation = (CocoonJSOrientation)[n integerValue];
    
    devAppOrientations = 0;
    if (orientation == ORIENTATION_LANDSCAPE || orientation == ORIENTATION_BOTH) {
        devAppOrientations |= UIInterfaceOrientationMaskLandscape;
    }
    if (orientation == ORIENTATION_PORTRAIT || orientation == ORIENTATION_BOTH) {
        devAppOrientations |= UIInterfaceOrientationMaskPortrait;
        if ( UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad ){
            devAppOrientations |= UIInterfaceOrientationMaskPortraitUpsideDown;
        }
    }
    
    //Mask devApp Orientations with orientations defined in the plist
    UIInterfaceOrientationMask plistOrientations = 0;
    NSArray * supported = [[NSBundle mainBundle] objectForInfoDictionaryKey:@"UISupportedInterfaceOrientations"];
    for (NSString * str in supported) {
        if ([str isEqualToString:@"UIInterfaceOrientationPortrait"]) {
            plistOrientations |= UIInterfaceOrientationMaskPortrait;
        }
        else if([str isEqualToString:@"UIInterfaceOrientationLandscapeLeft"]) {
            plistOrientations |= UIInterfaceOrientationMaskLandscapeLeft;
        }
        else if([str isEqualToString:@"UIInterfaceOrientationLandscapeRight"]) {
            plistOrientations |= UIInterfaceOrientationMaskLandscapeRight;
        }
        else if([str isEqualToString:@"UIInterfaceOrientationPortraitUpsideDown"]) {
            plistOrientations |= UIInterfaceOrientationMaskPortraitUpsideDown;
        }
    }
    devAppOrientations &= plistOrientations;
}


-(void) debugClick: (id) sender
{
    if (engine == ENGINE_WEBVIEW_PLUS) {
        WKWebView* view = (WKWebView*)[self.webViewEngine engineWebView];
        [view loadHTMLString:@"<html/>" baseURL:nil];
    }
    
    if (_cocoonDelegate) {
        [_cocoonDelegate cocoonCordovaVCDidExit:self];
    }
}

-(void) initDebug
{
    if (engine != ENGINE_CANVAS_PLUS) {
        UIButton * button = [UIButton buttonWithType:UIButtonTypeCustom];
        [button addTarget:self action:@selector(debugClick:) forControlEvents:UIControlEventTouchUpInside];
        button.frame = CGRectMake(0, 0, 40, 40);
        button.autoresizingMask = UIViewAutoresizingFlexibleRightMargin | UIViewAutoresizingFlexibleLeftMargin;
        UIImage * img = [UIImage imageNamed:@"devapp_exit@2x.png"];
        [button setBackgroundImage:img forState:UIControlStateNormal];
        [self.view addSubview:button];
    }
}

- (BOOL)prefersStatusBarHidden {
    return YES;
}

- (BOOL) shouldAutorotate
{
    return YES;
}

- (NSUInteger)supportedInterfaceOrientations
{
    if (devAppOrientations) {
        return devAppOrientations;
    }
    else {
        return [super supportedInterfaceOrientations];
    }
}


- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    if (devAppOrientations) {
        switch (interfaceOrientation) {
            default:
            case UIInterfaceOrientationLandscapeLeft: return devAppOrientations & UIInterfaceOrientationMaskLandscapeLeft;
            case UIInterfaceOrientationLandscapeRight: return devAppOrientations & UIInterfaceOrientationMaskLandscapeRight;
            case UIInterfaceOrientationPortrait: return devAppOrientations & UIInterfaceOrientationPortrait;
            case UIInterfaceOrientationPortraitUpsideDown: return devAppOrientations & UIInterfaceOrientationPortraitUpsideDown;
        }
        return YES;
    }
    else {
        return [super shouldAutorotateToInterfaceOrientation:interfaceOrientation];
    }
    
}


#pragma mark UIWebDelegate implementation

- (void)webViewDidFinishLoad:(UIWebView*)theWebView
{
    // Black base color for background matches the native apps
    theWebView.backgroundColor = [UIColor blackColor];
    
    if (wbChainDelegate) {
        [wbChainDelegate webViewDidFinishLoad:theWebView];
    }
    
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidFinishLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidFinishLoad:self];
    }
}

- (void) webViewDidStartLoad:(UIWebView*)theWebView
{
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginResetNotification object:self.webView]];
    
    if (wbChainDelegate) {
        [wbChainDelegate webViewDidStartLoad:theWebView];
    }
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidStartLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidStartLoad:self];
    }
}

- (void) webView:(UIWebView*)theWebView didFailLoadWithError:(NSError*)error
{
    if (wbChainDelegate) {
        [wbChainDelegate webView:theWebView didFailLoadWithError:error];
    }
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVC:didFailLoadWithError:)]) {
        [_cocoonDelegate cocoonCordovaVC:self didFailLoadWithError:error];
    }
}

- (BOOL)webView:(UIWebView*)theWebView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType
{
    if (wbChainDelegate) {
        [wbChainDelegate webView:theWebView shouldStartLoadWithRequest:request navigationType:navigationType];
    }
    
    if ([request.URL.scheme isEqualToString:@"gap"]) {
        return  NO;
    }
    
    return YES; //allow everything in the devapp
}

#pragma mark WKNavigationDelegate

- (void)webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation
{
    if (wkChainDelegate) {
        [wkChainDelegate webView:(WKWebView *)webView didStartProvisionalNavigation:(WKNavigation *)navigation];
    }
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidStartLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidStartLoad:self];
    }
}

- (void)webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation
{
    if (wkChainDelegate) {
        [wkChainDelegate webView:(WKWebView *)webView didFinishNavigation:(WKNavigation *)navigation];
    }
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidFinishLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidFinishLoad:self];
    }
}

- (void)webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation withError:(NSError *)error
{
    if (wkChainDelegate) {
        [wkChainDelegate webView:(WKWebView *)webView didFailNavigation:(WKNavigation *)navigation withError:error];
    }
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVC:didFailLoadWithError:)]) {
        [_cocoonDelegate cocoonCordovaVC:self didFailLoadWithError:error];
    }
}

- (void)webView:(WKWebView *)webView didReceiveAuthenticationChallenge:(NSURLAuthenticationChallenge *)challenge completionHandler:(void (^)(NSURLSessionAuthChallengeDisposition disposition, NSURLCredential *credential))completionHandler
{
    completionHandler(NSURLSessionAuthChallengeUseCredential, nil);
}

- (void) webView: (WKWebView *) webView decidePolicyForNavigationAction: (WKNavigationAction*) navigationAction decisionHandler: (void (^)(WKNavigationActionPolicy)) decisionHandler
{
    decisionHandler(WKNavigationActionPolicyAllow);
}

#pragma mark CocoonJS

- (void)cocoonJSViewDidStartLoad:(UIView*)view
{
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPluginResetNotification object:self.webView]];
    
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidStartLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidStartLoad:self];
    }
}

- (void)cocoonJSViewDidFinishLoad:(UIView*) view withRuntimeName:(NSString*)runtimeName;
{
    [[NSNotificationCenter defaultCenter] postNotification:[NSNotification notificationWithName:CDVPageDidLoadNotification object:self.webView]];
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCDidFinishLoad:)]) {
        [_cocoonDelegate cocoonCordovaVCDidFinishLoad:self];
    }
}

- (void)cocoonJSView:(UIView*)view didFailLoadWithError:(NSError*)error andRuntimeName:(NSString*)runtimeName;
{
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVC:didFailLoadWithError:)]) {
        [_cocoonDelegate cocoonCordovaVC:self didFailLoadWithError:error];
    }
}

- (void)cocoonJSViewDidExit:(UIView*)view
{
    if (_cocoonDelegate) {
        [_cocoonDelegate cocoonCordovaVCDidExit:self];
    }
}

-(BOOL)cocoonJSViewWillReload:(UIView*) view
{
    if (_cocoonDelegate && [_cocoonDelegate respondsToSelector:@selector(cocoonCordovaVCWillReload:)]) {
        return [_cocoonDelegate cocoonCordovaVCWillReload:self];
    }
    return NO;
}

@end
