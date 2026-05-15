#!/usr/bin/env python3
"""验证 JAR 中的 migration 文件 CRC 与源码一致"""
import zipfile, zlib, os, sys

jar_path = sys.argv[1]
src_dir = sys.argv[2]

with zipfile.ZipFile(jar_path) as z:
    jar_migs = {}
    for name in sorted(z.namelist()):
        if 'db/migration/V' in name and name.endswith('.sql'):
            data = z.read(name)
            crc = zlib.crc32(data) & 0xffffffff
            fname = name.split('/')[-1]
            jar_migs[fname] = crc

src_crc = {}
for fname in sorted(os.listdir(src_dir)):
    if fname.startswith('V') and fname.endswith('.sql'):
        path = os.path.join(src_dir, fname)
        with open(path, 'rb') as f:
            crc = zlib.crc32(f.read()) & 0xffffffff
        src_crc[fname] = crc

errors = []
for fname in sorted(set(list(jar_migs.keys()) + list(src_crc.keys()))):
    src = src_crc.get(fname, 'MISSING')
    jar = jar_migs.get(fname, 'MISSING')
    if src != jar:
        errors.append(f"  {fname}: source={src}, jar={jar}")

if errors:
    print("ERROR: CRC 不匹配:")
    for e in errors:
        print(e)
    sys.exit(1)
else:
    all_files = sorted(src_crc.keys())
    print(f"  所有 {len(all_files)} 个 migration 文件 CRC 校验通过")
    for f in all_files:
        print(f"    {f}: {src_crc[f]}")
