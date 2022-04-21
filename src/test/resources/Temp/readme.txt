Order

1 patient.txt
2 chv_provder.txt
3 claimss.txt

after that any order
chv_rcp_mgcare-1.txt
chv_rcp_spec_pgm-1.txt


patient.txt

curl  --request POST 'https://mdix-mmisservices1.nicheaimlabs.com/mdmi/transformation/transformAndPost?source=NJ.Person&target=FHIRR4JSON.MasterBundle' --header 'Content-Type: application/xml' -d @patient.txt

chv_provder.txt

curl  --request POST 'https://mdix-mmisservices2.nicheaimlabs.com/mdmi/transformation/transformAndPost?source=NJ.PROVIDER&target=FHIRR4JSON.MasterBundle' --header 'Content-Type: application/xml' -d @chv_provder.txt

claimss.txt

curl  --request POST 'https://mdix-mmisservices2.nicheaimlabs.com/mdmi/transformation/transformAndPost?source=NJ.Claim&target=FHIRR4JSON.MasterBundle' --header 'Content-Type: application/xml' -d @claimss.txt

chv_rcp_mgcare-1.txt

curl  --request POST 'https://mdix-mmisservices1.nicheaimlabs.com/mdmi/transformation/transformAndPost?source=NJ.RCP_SPEC_PGM&target=FHIRR4JSON.MasterBundle' --header 'Content-Type: application/xml' -d @chv_rcp_mgcare-1.txt

chv_rcp_spec_pgm-1.txt

curl  --request POST 'https://mdix-mmisservices1.nicheaimlabs.com/mdmi/transformation/transformAndPost?source=NJ.RCP_SPEC_PGM&target=FHIRR4JSON.MasterBundle' --header 'Content-Type: application/xml' -d @chv_rcp_spec_pgm-1.txt

