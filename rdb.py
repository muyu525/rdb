#!/usr/bin/env python

# coding: utf-8


import time
import sys
import os
import json
import socket


WORKER_PULL = 0x03010101
WORKER_INSTALL = 0x03010103

TAG_START = 0x01010101
TAG_DIR = 0x01010103
TAG_DATA = 0x01010113
TAG_END = 0x01011101
TAG_CLOSE = 0x01010301

BLOCK_SIZE = 512 * 1024


SERVER = '192.168.1.103'
PORT = 8080

def getTag(data):
    return int.from_bytes(data, byteorder='big')

def getSize(data):
    return int.from_bytes(data, byteorder='big')


def makeTag(tag):
    result = tag.to_bytes(4, byteorder='big')

    return result


def makeSize(size):
    result = size.to_bytes(4, byteorder='big')

    return result


def makeInfo(name, size):
    info = dict()
    info['packageName'] = name
    info['size'] = size
    info['blockSize'] = BLOCK_SIZE
    info['md5'] = ''

    return json.dumps(info).encode()



def showPercent(sent_size, apk_size):
    percent = int(sent_size / apk_size * 100)
    if percent < 100:
        print('%02d%%\b\b\b' % percent, end='')
    else:
        print('%d%%' % percent)
    sys.stdout.flush()

def install(apk):
    apk_name = os.path.basename(apk)
    apk_size = os.path.getsize(apk)

    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((SERVER, PORT))

    print('connected...')

    tag = makeTag(WORKER_INSTALL)
    sent = s.sendall(tag)

    tag = makeTag(TAG_START)
    info = makeInfo(apk_name, apk_size)
    info_size = len(info)
    size = makeSize(info_size)

    result = b''.join([tag, size, info])
    sent = s.sendall(result)


    count = int(apk_size / BLOCK_SIZE)
    rev = apk_size % BLOCK_SIZE
    print('packet:', count, ',rev:', rev)


    index = 0
    tag = makeTag(TAG_DATA)
    sent_size = 0
    with open(apk, mode='rb') as f:
        while index < count:
            data = f.read(BLOCK_SIZE)
            size = makeSize(BLOCK_SIZE)
            result = b''.join([tag, size, data])
            sent = s.sendall(result)
            sent_size = sent_size + BLOCK_SIZE
            showPercent(sent_size, apk_size)

            index = index + 1

        if rev > 0:
            data = f.read(rev)
            size = makeSize(rev)
            result = b''.join([tag, size, data])
            sent = s.sendall(result)
            sent_size = sent_size + rev
            showPercent(sent_size, apk_size)


    tag = makeTag(TAG_END)
    size = makeSize(0)
    result = b''.join([tag, size])
    sent = s.sendall(result)

    time.sleep(1)
    print('send over')


def receive(sock, size):
    chunks = []
    bytes_recd = 0
    while bytes_recd < size:
        chunk = sock.recv(min(size- bytes_recd, 2048))
        if chunk == b'':
            raise RuntimeError("socket connection broken")
        chunks.append(chunk)
        bytes_recd = bytes_recd + len(chunk)
    return b''.join(chunks)

def pull(target, save):
    print('try to connect:%s' % SERVER)
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    s.connect((SERVER, PORT))
    print('connected...')

    tag = makeTag(WORKER_PULL)
    sent = s.sendall(tag)

    tag = makeTag(TAG_START)
    info = target.encode()
    info_size = len(info)
    size = makeSize(info_size)

    result = b''.join([tag, size, info])
    sent = s.sendall(result)


    ofile = None
    while True:
        #print('try to recv tag')
        data = receive(s, 8)

        tag = getTag(data[:4])
        size = getSize(data[4:8])

        if tag == TAG_DIR:
            if size > 0:
                path = receive(s, size).decode()
                save_path = os.path.join(save, path)
                print('dir:%s' % save_path)
                if os.path.isdir(save_path):
                    pass
                else:
                    os.makedirs(save_path)
        elif tag == TAG_START:
            file = receive(s, size).decode()
            print('file: %s' % file)
            save_file = os.path.join(save, file)
            ofile = open(save_file, 'wb')
        elif tag == TAG_DATA:
            data = receive(s, size)
            ofile.write(data)
        elif tag == TAG_END:
            ofile.close()
            ofile = None
        elif tag == TAG_CLOSE:
            print('connect close')
            break





if '__main__' == __name__:
    command = sys.argv[1]
    if command == 'install':
        apk = sys.argv[2]
        install(apk)
    elif command == 'pull':
        target = sys.argv[2]
        save = sys.argv[3]
        pull(target, save)

