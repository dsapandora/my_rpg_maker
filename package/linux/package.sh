#!/bin/sh
set -v
echo "Packaging for Linux/Mac..."
echo "======================"

export SRC_DIR=$(cd "$(dirname "$0")"; pwd)

rm -rfv $SRC_DIR/target/*
export ARCHIVE_PATH=$SRC_DIR/target/myrpgmaker-$VERSION
mkdir -p $ARCHIVE_PATH
export JARPATH=$SRC_DIR/../../desktop/build/libs/desktop-1.0.jar

cp -v $JARPATH $ARCHIVE_PATH/editor-min.jar
cp -v $SRC_DIR/icon.png $ARCHIVE_PATH
cp -v $SRC_DIR/myrpgmaker.desktop $ARCHIVE_PATH
cp -v $SRC_DIR/launch.sh $ARCHIVE_PATH/myrpgmaker

chmod -v 755 $ARCHIVE_PATH/myrpgmaker

export ARCHIVE="myrpgmaker-${VERSION}.zip"

cd $SRC_DIR/target
zip -r $ARCHIVE myrpgmaker-$VERSION

cp -v $SRC_DIR/target/$ARCHIVE $SRC_DIR/../target/myrpgmaker-$VERSION-linux-mac.zip

echo ""
