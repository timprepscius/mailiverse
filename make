if [ -z "$1" ]; then echo "Must supply version"; exit 0; fi

pushd james/apache-james-mailbox-memory
./build
popd

pushd james/james-trunk/protocols-smtp/
./build
popd

pushd build
./make $1
popd

