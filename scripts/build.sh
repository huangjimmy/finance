git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.14.0
alias asdf="$HOME/.asdf/asdf.sh"
asdf plugin add nodejs https://github.com/asdf-vm/asdf-nodejs.git
asdf install
npm install
./gradlew -i clean
./gradlew -i update
./gradlew -i jooqCodegen
./gradlew -i build