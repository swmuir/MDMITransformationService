import os
from datetime import datetime
from io import StringIO
from shutil import copyfileobj
from time import sleep

# import google.auth
import requests
from google.cloud import storage

import sys

# import logging

# Imports the Cloud Logging client library
# import google.cloud.logging
#
# # Instantiates a client
# client = google.cloud.logging.Client()
#
# # Retrieves a Cloud Logging handler based on the environment
# # you're running in and integrates the handler with the
# # Python logging module. By default this captures all logs
# # at INFO level and higher
# client.setup_logging()

# import auxiliary_module

# creds, project = google.auth.default()
# auth_req = google.auth.transport.requests.Request()
# creds.refresh(auth_req)

# MGCare_Sample_1.txt
# SPEC_PGM_Sample_1.txt
# Claim_Sample_1.txt
# Patient_Sample_1.txt
# Provider_Organization_Sample_1.txt
# Provider_Practitioner_Sample_1.txt


# prod-pcma-complexity-score/mmis/Sample_Files/chv_provider.txt

MDIX1 = os.getenv("MDIX1")
MDIX2 = os.getenv("MDIX2")
BUCKET = os.getenv("BUCKET")
BATCH = 50

ORDERED_FILES1 = [
 
    (
        "Patient_Sample_1.txt",
        f"{MDIX1}/mdmi/transformation/transformAndPost?source=NJ.Person&target=FHIRR4JSON.MasterBundle",
    )
    
   
   
 
    
]
 


ORDERED_FILES2 = [
 
    
    (
        "Provider_Organization_Sample_1.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.PROVIDER&target=FHIRR4JSON.MasterBundle",
    ),
    (
        "Provider_Practitioner_Sample_1.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.PROVIDER&target=FHIRR4JSON.MasterBundle",
    ) 
 
    
]

ORDERED_FILES2B = [   
    (
        "chv_provider.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.PROVIDER&target=FHIRR4JSON.MasterBundle",
    )
    
]

ORDERED_FILES3 = [
 
    
    (
        "Claim_Sample_1.txt",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.Claim&target=FHIRR4JSON.MasterBundle",
    ) 
 
    
]

ORDERED_FILES3a = [
 
    
    (
        "step3_claims_part453.csv",
        f"{MDIX2}/mdmi/transformation/transformAndPost?source=NJ.Claim&target=FHIRR4JSON.MasterBundle",
    ) 
 
    
]

# //Buckets

# prod-mmis-temp

# step3_claims_part453.csv


ORDERED_FILES4 = [
 
    
    
    
    
        (
        "MGCare_Sample_1.txt",
        f"{MDIX1}/mdmi/transformation/transformAndPost?source=NJ.RCPMGCARE&target=FHIRR4JSON.MasterBundle",
    ),
    (
        "SPEC_PGM_Sample_1.txt",
        f"{MDIX1}/mdmi/transformation/transformAndPost?source=NJ.RCP_SPEC_PGM&target=FHIRR4JSON.MasterBundle",
    ),
 
    
]

ORDERED_FILES4a = [
 
    
    
    
    
        (
        "MGCare_Sample_1.txt",
        f"{MDIX1}/mdmi/transformation/transformAndPost?source=NJ.RCPMGCARE&target=FHIRR4JSON.MasterBundle",
    ) 
    
]
 

def main(source_file_bucket: str = None):

   # Imports the Google Cloud client library
    from google.cloud import logging

    # Instantiates a client
    logging_client = logging.Client()

    # The name of the log to write to
    log_name = "mdmi_mmis"
    # Selects the log to write to
    logger = logging_client.logger(log_name)

    # The data to log
    text = "Hello, world!ssssssssssssss"

    # Writes the log entry
    logger.log_text(text)

    print("Logged: {}".format(text))
    # [END logging_quickstart]    
 
# # Instantiates a client
#     logging_client = logging.Client()
#
# # The name of the log to write to
#     log_name = "mdmi_mmis"
# # Selects the log to write to
#     logger = logging_client.logger(log_name)
#
# # The data to log
#     text = "Hello, world!"
#
# # Writes the log entry
#     logger.log_text(text)
#
#     print("Logged: {}".format(text))
#


# # create logger with 'spam_application'
#     logger = logging.getLogger('spam_application')
#     logger.setLevel(logging.DEBUG)
# # create file handler which logs even debug messages
#     fh = logging.FileHandler('spam.log')
#     fh.setLevel(logging.DEBUG)
# # create console handler with a higher log level
#     ch = logging.StreamHandler()
#     ch.setLevel(logging.ERROR)
# # create formatter and add it to the handlers
#     formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
#     fh.setFormatter(formatter)
#     ch.setFormatter(formatter)
# # add the handlers to the logger
#     logger.addHandler(fh)
#     logger.addHandler(ch)
#
#     logger.info('creating an instance of auxiliary_module.Auxiliary')
#     # a = auxiliary_module.Auxiliary()
#     logger.info('created an instance of auxiliary_module.Auxiliary')
#     logger.info('calling auxiliary_module.Auxiliary.do_something')
#     # a.do_something()
#     logger.info('finished auxiliary_module.Auxiliary.do_something')
#     logger.info('calling auxiliary_module.some_function()')
#     # auxiliary_module.some_function()
#     logger.info('done with auxiliary_module.some_function()')
#

    
    
    ts = datetime.utcnow().isoformat()
    storage_client = storage.Client()
    bucket = storage_client.bucket(source_file_bucket)
    for file_name, url in ORDERED_FILES4:
        blob = bucket.blob(f"mmis/Sample_Files/{file_name}") 
        blob.download_to_filename("/tmp/temp.csv")
        with open("/tmp/temp.csv", "r") as txt_file:
            lines = txt_file.readlines()
            header = lines.pop(0).replace("#", "")
            upload_file = StringIO()
            upload_file.write(header)
            urlist_len = len(lines)-1
            for n, line in enumerate(lines):
                # upload_file.write(line.replace("\\", "").replace(">", "&lt;"))
                upload_file.write(line)
                if n and (n % BATCH == 0 or n ==urlist_len):
                    print(f"{file_name} uploading {n-BATCH} - {n}")
                    while True:  # retry
                        upload_file.seek(0)
                        upload_file_copy = StringIO()
                        copyfileobj(upload_file, upload_file_copy)
                        upload_file_copy.seek(0)
                        
                        # sys.stdout.write('gfg')
                        # sys.stdout.write(upload_file_copy.getvalue())
                        # sys.stdout.write('\n')
                        # sys.stdout.write('for geeks')

                        response = requests.post(
                            url,
                            # headers={"Content-Type": "application/xml"},
                            files={"message": upload_file_copy},
                        )
                        # print(response.json())
                        
                        logger.log(response.json())
                         
                        if response.ok:                          
                            # sys.stdout.write('\n')
                            break
                        else:
                            logger.error(response.json())
                            logger.error(upload_file_copy.getvalue())
                            
                            # print(f"retry uploading {n-BATCH} - {n}")
                            # sleep(1)
                        # else:
                            # raise Exception
                    upload_file = StringIO()
                    upload_file.write(header)
    # for file_name, url in ORDERED_FILES:
    #     blob = bucket.blob(f"mmis/{file_name}")
    #     blob_copy = bucket.copy_blob(blob, bucket, f"mmis/processed/{ts}/{file_name}")
    #     bucket.delete_blob(f"mmis/{file_name}")
    return None


if __name__ == "__main__":
    main(BUCKET)
