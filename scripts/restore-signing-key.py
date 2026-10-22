#!/usr/bin/env python3
"""Restore an authenticated, password-encrypted Duong Ngang signing backup.

Requires Python 3 and cryptography. Passwords and raw key material are never printed.
Restores into a NEW directory and refuses overwriting existing files.
"""
import argparse
import base64
import getpass
import hashlib
import json
import os
from pathlib import Path

from cryptography.exceptions import InvalidTag
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers.aead import AESGCM
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

FORMAT = 'duong-ngang-signing-backup-v1'
AAD = FORMAT.encode('ascii')
ITERATIONS = 600000
CERTIFICATE = '281d109c85d7c1d4cb6dc88ed1e69972f1a44cbc74f368991c7d93fae7c18e44'


def decode(value):
    return base64.b64decode(value, validate=True)


def decrypt(path, password):
    if path.stat().st_size > 65536:
        raise ValueError('Backup is unexpectedly large')
    envelope = json.loads(path.read_bytes())
    if (envelope.get('format') != FORMAT or envelope.get('iterations') != ITERATIONS
            or envelope.get('cipher') != 'AES-256-GCM'
            or envelope.get('kdf') != 'PBKDF2-HMAC-SHA256'):
        raise ValueError('Unsupported backup format')
    salt, nonce = decode(envelope['salt']), decode(envelope['nonce'])
    if len(salt) != 16 or len(nonce) != 12:
        raise ValueError('Invalid salt or nonce')
    key = PBKDF2HMAC(algorithm=hashes.SHA256(), length=32, salt=salt,
                    iterations=ITERATIONS).derive(password)
    bundle = json.loads(AESGCM(key).decrypt(nonce, decode(envelope['ciphertext']), AAD))
    raw = decode(bundle['keystore'])
    info = bundle['signing_info']
    if hashlib.sha256(raw).hexdigest() != info['keystore_sha256']:
        raise ValueError('Key checksum mismatch')
    if info['certificate_sha256'] != CERTIFICATE:
        raise ValueError('Certificate does not match this app')
    return raw, info


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('backup', type=Path)
    parser.add_argument('--output-dir', required=True, type=Path)
    parser.add_argument('--password-file', type=Path,
                        help='Private local password file; otherwise prompts without echo')
    args = parser.parse_args()
    password = (args.password_file.read_bytes().rstrip(b'\r\n') if args.password_file
                else getpass.getpass('Backup password: ').encode('utf-8'))
    try:
        raw, info = decrypt(args.backup, password)
    except InvalidTag:
        raise SystemExit('Wrong password or altered backup; nothing was restored.')
    # Decrypt/authenticate before creating a directory; never overwrite an existing key.
    args.output_dir.mkdir(mode=0o700, parents=False, exist_ok=False)
    for filename, data in [('debug.keystore', raw),
                           ('signing-info.json', json.dumps(info, indent=2).encode('utf-8'))]:
        fd = os.open(args.output_dir / filename, os.O_WRONLY | os.O_CREAT | os.O_EXCL, 0o600)
        with os.fdopen(fd, 'wb') as handle:
            handle.write(data)
    print('Restored successfully into a new private directory.')
    print('Certificate SHA-256:', info['certificate_sha256'])


if __name__ == '__main__':
    main()
