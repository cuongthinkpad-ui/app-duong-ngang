#!/usr/bin/env python3
"""Generate secrets to a PRIVATE directory supplied by operator; never print contents."""
import base64,os,secrets,sys
from pathlib import Path
from cryptography.hazmat.primitives.asymmetric import ec
from cryptography.hazmat.primitives import serialization
if len(sys.argv)!=2:raise SystemExit('Usage: python deploy/generate-secrets.py /PRIVATE/directory')
p=Path(sys.argv[1]).resolve();root=Path(__file__).resolve().parents[1]
if p==root or p.is_relative_to(root):raise SystemExit('Use a directory OUTSIDE this source tree')
os.umask(0o077);p.mkdir(mode=0o700,parents=True,exist_ok=True)
key=ec.generate_private_key(ec.SECP256R1())
values={'app-key':base64.urlsafe_b64encode(secrets.token_bytes(32)).rstrip(b'='),'vapid-private-key':key.private_bytes(serialization.Encoding.PEM,serialization.PrivateFormat.PKCS8,serialization.NoEncryption())}
for name,value in values.items():
    with (p/name).open('xb') as f:f.write(value)
print('Created private key files. Upload with gcloud secrets versions add --data-file; do not paste their contents.')
