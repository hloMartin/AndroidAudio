# OpusTools 源码 

build.sh 写死了 ndk 的路径，记得替换。

我是在 mac 上编译的，其他环境没有测试过。

构建出来的 so 在 jni 的同级目录 libs 下。

NOTE：经过测试发现，最好使用 ndk-r10e 版本进行编译 ，否则在部分机型上会有问题。