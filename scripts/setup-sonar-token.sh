#!/usr/bin/env sh
set -eu

host_url="http://localhost:9000"
token=""

while [ "$#" -gt 0 ]; do
    case "$1" in
        --token)
            token="${2:-}"
            shift 2
            ;;
        --host-url)
            host_url="${2:-}"
            shift 2
            ;;
        *)
            echo "Unknown argument: $1" >&2
            echo "Usage: $0 [--token squ_...] [--host-url http://localhost:9000]" >&2
            exit 1
            ;;
    esac
done

if [ -z "$token" ]; then
    printf "Sonar token: "
    trap 'stty echo' EXIT
    stty -echo
    read -r token
    stty echo
    trap - EXIT
    printf "\n"
fi

if [ -z "$token" ]; then
    echo "A Sonar token is required." >&2
    exit 1
fi

gradle_dir="$HOME/.gradle"
properties_path="$gradle_dir/gradle.properties"
mkdir -p "$gradle_dir"
touch "$properties_path"

tmp_file="$(mktemp)"
grep -v -E '^[[:space:]]*sonar\.(token|host\.url)[[:space:]]*=' "$properties_path" > "$tmp_file" || true

if [ -s "$tmp_file" ] && [ "$(tail -c 1 "$tmp_file" | wc -l | tr -d ' ')" = "0" ]; then
    printf "\n" >> "$tmp_file"
fi

printf "sonar.token=%s\n" "$token" >> "$tmp_file"
printf "sonar.host.url=%s\n" "$host_url" >> "$tmp_file"
mv "$tmp_file" "$properties_path"

echo "Sonar token configured in $properties_path"
echo "Run: ./gradlew sonar sonarQualityGate"
