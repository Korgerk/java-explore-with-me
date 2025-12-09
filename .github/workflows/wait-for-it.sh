#!/bin/bash
# wait-for-it.sh — TCP-only version compatible with databases

cmdname=$(basename "$0")

echoerr() { if [[ "${QUIET}" -ne 1 ]]; then echo "$@" 1>&2; fi; }

usage() {
  echo "Usage: $cmdname host:port [-- command args]" 1>&2
  exit 1
}

wait_for() {
  local host="$1"
  local port="$2"
  local shift_count=2
  local timeout="${TIMEOUT:-15}"

  if ! command -v nc >/dev/null; then
    echoerr "nc (netcat) is required but not installed."
    exit 1
  fi

  end_time=$(( $(date +%s) + timeout ))
  while :; do
    if nc -z "$host" "$port"; then
      echoerr "$cmdname: $host:$port is available"
      break
    fi
    if [[ $(date +%s) -ge $end_time ]]; then
      echoerr "$cmdname: timeout waiting for $host:$port"
      exit 1
    fi
    sleep 1
  done

  if [[ $# -gt $shift_count ]]; then
    shift $shift_count
    exec "$@"
  fi
}

if [[ $# -lt 1 ]]; then
  usage
fi

case "$1" in
  *:* )
    hostport=(${1//:/ })
    host=${hostport[0]}
    port=${hostport[1]}
    shift
    ;;
  * )
    echoerr "Error: expected host:port"
    usage
    ;;
esac

QUIET=0
TIMEOUT=15

while [[ $# -gt 0 ]]; do
  case "$1" in
    -q|--quiet)
      QUIET=1
      shift
      ;;
    -t|--timeout)
      TIMEOUT="$2"
      shift 2
      ;;
    --)
      shift
      break
      ;;
    *)
      break
      ;;
  esac
done

wait_for "$host" "$port" "$@"