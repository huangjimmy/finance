git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0
chmod +x ~/.asdf/asdf.sh
~/.asdf/asdf.sh plugin add nodejs https://github.com/asdf-vm/asdf-nodejs.git
~/.asdf/asdf.sh install
npm install
./gradlew -i clean
./gradlew -i update
./gradlew -i jooqCodegen
./gradlew -i build