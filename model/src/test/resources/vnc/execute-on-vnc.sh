#!/bin/bash

NEW_DISPLAY=42
DONE="no"

while [ "$DONE" = "no" ]; do
  out=$(xdpyinfo -display :${NEW_DISPLAY} 2>&1)
  if [[ "$out" == name* ]] || [[ "$out" == Invalid* ]]
  then
    # command succeeded; or failed with access error;  display exists
    (( NEW_DISPLAY+=1 ))
  else
    # display doesn't exist
    DONE="yes"
  fi
done

echo "Using first available display :${NEW_DISPLAY}"

mkdir "$HOME/.vnc/"
vncpasswd -f > "$HOME/.vnc/passwd" <<EOF
123456
123456
EOF

chmod 600 "$HOME/.vnc/passwd"

OLD_DISPLAY=${DISPLAY}
vncserver ":${NEW_DISPLAY}" -localhost -geometry 1024x780 -depth 16
export DISPLAY=:${NEW_DISPLAY}

echo MAVEN_NASHORN="${MAVEN_NASHORN}"
echo MAVEN_BYTEBUDDY="${MAVEN_BYTEBUDDY}"

"$@"
JSQL_EXIT_CODE=$?
echo "# Container command exit code $JSQL_EXIT_CODE"

export DISPLAY=${OLD_DISPLAY}
vncserver -kill ":${NEW_DISPLAY}"

exit $JSQL_EXIT_CODE