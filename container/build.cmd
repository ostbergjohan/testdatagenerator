wsl podman build -t ocp4-quay.arbetsformedlingen.se/testplattformar/testdatagen:latest . --tls-verify=false
wsl podman login -u="testplattformar+prestandatest" -p="ULI3SHG1RXBDW6IWF841NQS2ABOTTOAUT1JRCXDLHAV7QHHTISGTZYCU713Z11M6" ocp4-quay.arbetsformedlingen.se --tls-verify=false
wsl podman push ocp4-quay.arbetsformedlingen.se/testplattformar/testdatagen:latest --tls-verify=false
pause
