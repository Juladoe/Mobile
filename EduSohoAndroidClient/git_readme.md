# git开发分支提交规范

## 分支解释

|分支名称|作用|备注|
|:-:|:-:|:----|
|master|一直保持产品稳定|永远处在即将发布(production-ready)状态|
|develop|最新的开发状态||
|release|发布版本分支|基于develop或者master, 完成后merge回develop和master|
|fix|修复bug分支|比如:fix/xxx-login，xxx是redmine上的bug号
|feature|迭代某一新功能|在develop分支上分出来，比如：feature/replace_new_find_module|
|custom|客户订制||


## 迭代功能开发流程

1. 当前的迭代任务从develop上建分支，如feature/xxxx
2. 如果条件允许可以要求其他人review
3. 自测完毕merge入develop


## bug修复
一般从最新发布的版本中去修复，最新版本和master是同步的，所以可以用master分支中建立

1. 从master上建新分支，如fix/login_error，
2. 同时merge到develop和master分支上

## 版本发布
1. 如果是紧急修复bug，并无新功能，从master中建立release/x.x.x发布，并在master上打tag
2. 如果是有新功能的，从develop中建立建立release/x.x.x发布，并在master上打tag

## custom客户订制分支

1. 可以从release/x.x.x版本中建立，命名是custom/xxxxxx

[参考](http://nvie.com/posts/a-successful-git-branching-model/)

上传fir
./gradlew publishApkEdusohoDebug //上传debug版本
./gradlew publishApkEdusohoRelease //上传release版本