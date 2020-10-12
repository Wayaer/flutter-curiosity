#import "GalleryTools.h"
#import "TZImageManager.h"
#import "TZImageCropManager.h"
#import "NativeTools.h"
#import <AssetsLibrary/AssetsLibrary.h>

@implementation GalleryTools

NSString * const curiosityCaches =@"CuriosityCaches";

+ (void)openImagePicker:(FlutterMethodCall*)call :(UIViewController*)viewController :(FlutterResult)result{
    NSDictionary * arguments = call.arguments;
    int maxSelectNum = [[arguments objectForKey:@"maxSelectNum"] intValue];
    int minSelectNum = [[arguments objectForKey:@"minSelectNum"] intValue];
    int videoMaxSecond = [[arguments objectForKey:@"videoMaxSecond"] intValue];
    int pickerSelectType = [[arguments objectForKey:@"pickerSelectType"] intValue];
    BOOL previewImage = [[arguments objectForKey:@"previewImage"] boolValue];
    BOOL isCamera = [[arguments objectForKey:@"isCamera"] boolValue];
    BOOL isGif = [[arguments objectForKey:@"isGif"] boolValue];
    BOOL originalPhoto = [[arguments objectForKey:@"originalPhoto"] boolValue];
    
    TZImagePickerController *picker = [[TZImagePickerController alloc] initWithMaxImagesCount:maxSelectNum delegate:nil];
    picker.maxImagesCount=maxSelectNum;
    picker.minImagesCount=minSelectNum;
    picker.allowCrop=false;
    if(pickerSelectType==1){
        picker.allowPickingImage=true;
        picker.allowTakePicture=isCamera;
        picker.allowPickingVideo=false;
        picker.allowTakeVideo=false;
        picker.allowPickingGif=isGif;
    }else if(pickerSelectType==2){
        picker.allowPickingVideo=true;
        picker.allowTakeVideo=isCamera;
        picker.allowPickingImage=false;
        picker.allowTakePicture=false;
    }else{
        picker.allowPickingGif=isGif;
        picker.allowPickingImage=true;
        picker.allowPickingVideo=true;
        picker.allowTakePicture=isCamera;
        picker.allowTakeVideo=isCamera;
    }
    if (isCamera&&picker.allowTakeVideo)picker.videoMaximumDuration=videoMaxSecond;
    picker.allowPreview=previewImage;
    picker.allowPickingOriginalPhoto=originalPhoto;
    picker.showPhotoCannotSelectLayer=true;
    if (maxSelectNum == 1) {  // 单选模式
        picker.showSelectBtn = YES;
        picker.maxImagesCount=1;
    }
    TZImageManager *manager= [TZImageManager manager];
    [picker setDidFinishPickingPhotosHandle:^(NSArray<UIImage *> *photos, NSArray *assets, BOOL isSelectOriginalPhoto) {
        NSMutableArray *selected = [NSMutableArray array];
        [assets enumerateObjectsUsingBlock:^(PHAsset* _Nonnull asset, NSUInteger index, BOOL * _Nonnull stop) {
            if (asset.mediaType == PHAssetMediaTypeVideo) {
                [manager getVideoOutputPathWithAsset:asset presetName:AVAssetExportPresetHighestQuality success:^(NSString *outputPath) {
                    [selected addObject:[self resultVideo:outputPath :asset :photos[index]]];
                    if(index + 1 == [assets count]) result(selected);
                } failure:^(NSString *errorMessage, NSError *error) {
                }];
            } else {
                BOOL isGIF = [manager getAssetType:asset] == TZAssetModelMediaTypePhotoGif;
                [manager requestImageDataForAsset:asset completion:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                    [selected addObject:[self resultPhoto:imageData phAsset:asset isGIF:isGIF]];
                    if(index + 1 == [assets count])result(selected);
                } progressHandler:^(double progress, NSError *error, BOOL *stop, NSDictionary *info) {
                }];
            }
        }];
        
    }];
    [viewController presentViewController:picker animated:YES completion:nil];
}


+ (void)deleteCacheDirFile:(FlutterResult)result{
    NSError *error;
    NSString *dir = [NSString stringWithFormat:@"%@CuriosityCaches/", NSTemporaryDirectory()];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    [fileManager removeItemAtPath:dir error:&error];
    result(error?[Tools resultFail]:[Tools resultSuccess]);
}
// 处理图片数组
+ (NSDictionary *)resultPhoto:(NSData *)data phAsset:(PHAsset *)asset isGIF:(BOOL)isGIF {
    [self createCache];
    NSMutableDictionary *photo  = [NSMutableDictionary dictionary];
    NSString *fileName = [NSString stringWithFormat:@"%@%@", [[NSUUID UUID] UUIDString], [asset valueForKey:@"filename"]];
    NSData *writeData = data;
    NSMutableString *filePath = [NSMutableString string];
    UIImage * image = isGIF?[UIImage sd_tz_animatedGIFWithData:data]:[UIImage imageWithData: data];
    [filePath appendString:[NSString stringWithFormat:@"%@CuriosityCaches/%@", NSTemporaryDirectory(), fileName]];
    [writeData writeToFile:filePath atomically:YES];
    photo[@"fileName"]  = fileName;
    photo[@"path"]      = filePath;
    photo[@"width"]     = [NSNumber numberWithDouble:image.size.width];
    photo[@"height"]    = [NSNumber numberWithDouble:image.size.height];
    NSInteger size      = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath error:nil].fileSize;
    photo[@"size"]      = [NSNumber numberWithDouble:size];
    photo[@"mediaType"] = @(asset.mediaType);
    return photo;
}

// 视频数据
+ (NSDictionary *)resultVideo:(NSString *)outputPath
                             :(PHAsset *)asset
                             :(UIImage *)coverImage{
    NSMutableDictionary *video = [NSMutableDictionary dictionary];
    video[@"path"] = outputPath;
    NSInteger size = [[NSFileManager defaultManager] attributesOfItemAtPath:outputPath error:nil].fileSize;
    video[@"size"] = @(size);
    video[@"width"] = @(asset.pixelWidth);
    video[@"height"] = @(asset.pixelHeight);
    video[@"duration"] = @(asset.duration);
    video[@"mediaType"] = @(asset.mediaType);
    return video;
}
/// 创建缓存目录
+ (BOOL)createCache {
    NSString * path = [NSString stringWithFormat:@"%@CuriosityCaches", NSTemporaryDirectory()];
    NSFileManager *fileManager = [NSFileManager defaultManager];
    BOOL isDir;
    if  (![fileManager fileExistsAtPath:path isDirectory:&isDir]) {
        //先判断目录是否存在，不存在才创建
        return [fileManager createDirectoryAtPath:path withIntermediateDirectories:YES attributes:nil error:nil];
    } else return NO;
}



// 打开相机
+ (void) openSystemCamera:(UIViewController *)viewController
                         :(UIImagePickerController *)picker
                         :(FlutterResult) result{
    AVAuthorizationStatus authStatus = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    if (authStatus == AVAuthorizationStatusRestricted || authStatus == AVAuthorizationStatusDenied) {
        //无相机权限 做一个友好的提示
        result([Tools resultInfo:@"No camera permission"]);
    } else if (authStatus == AVAuthorizationStatusNotDetermined) {
        // fix issue 466, 防止用户首次拍照拒绝授权时相机页黑屏
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            if (granted) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [GalleryTools openSystemCamera:viewController :picker :result];
                });
            }
        }];
        // 拍照之前还需要检查相册权限
    } else if ([PHPhotoLibrary authorizationStatus] == 2) {
        // 已被拒绝，没有相册权限，将无法保存拍的照片
        result([Tools resultInfo:@"Can't open camera, No photo album permission"]);
    } else if ([PHPhotoLibrary authorizationStatus] == 0) {
        // 未请求过相册权限
        [[TZImageManager manager] requestAuthorizationWithCompletion:^{
            [GalleryTools openSystemCamera:viewController :picker :result];
        }];
    } else {
        picker.allowsEditing = YES; //可编辑
        //判断是否可以打开照相机
        if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeCamera]){
            //摄像头
            picker.sourceType = UIImagePickerControllerSourceTypeCamera;
            [viewController presentViewController:picker animated:YES completion:nil];
        }else{
            result([Tools resultInfo:@"Can't open camera"]);
            
        }
    }
}


// 打开相册
+ (void) openSystemGallery:(UIViewController *)viewController :(UIImagePickerController *)picker :(FlutterResult) result{
    if([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypePhotoLibrary])  {
        picker.allowsEditing = YES;
        picker.sourceType = UIImagePickerControllerSourceTypePhotoLibrary;
        [viewController presentViewController:picker animated:YES completion: nil];
    }else{
        result(@"fail,Can't open album");
    }
}

@end

