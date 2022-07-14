# MMIS import

open those files "patient.txt", "chv_provider.txt", "claimss.txt",  "chv_rcp_mgcare.txt", "chv_rcp_spec_pgm.txt"   cut it in 1000 lines parts and send it to the respective service.

## install requirements

``` bash
pip install requirements.txt
```

## envvars

``` bash
export MDIX1=http://localhost:8080/
export MDIX2=http://localhost:8080/ 
export BUCKET=develop-pcma-complexity-score 
```

## run

``` bash
python mmis_import.py
```
