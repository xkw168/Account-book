#!usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import socket  # 用于获取IP地址等

from flask import Flask, jsonify  # 数据以JSON格式返回
from flask import request
from flask import send_from_directory

from sql import *

app = Flask(__name__, static_url_path='')
dir_path = os.path.join(app.root_path)


# JSON Test
@app.route('/jsonTest', methods=['POST'])
def json_test():
    json_data = request.form.get('jsonData')
    print(json_data)
    return jsonify({'data': json_data})


@app.errorhandler(404)
def page_not_found(e):
    res = jsonify({'error': 'not found'})
    res.status_code = 404
    return res


# 解决图标问题
@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'),
                               'favicon.ico', mimetype='image/vnd.microsoft.icon')


@app.route('/data', methods=['GET'])
def download_db():
    return send_from_directory(dir_path, "Account.db", as_attachment=True)


# 网站默认首页
@app.route('/', methods=['GET'])
def home():
    return "copyright xkw"


# 输入ok
@app.route('/yingzi', methods=['GET'])
def ok():
    return "萨瓦迪卡，迎紫高!"


# 输入字符
@app.route('/<name>', methods=['GET'])
def hello(name):
    return "萨瓦迪卡，" + name + "!"


@app.route('/queryAllAccount/<offset>/<limit>', methods=['GET'])
def query_all_account(offset, limit):
    accounts = query_all_account_info(int(offset), int(limit))
    response = {"accounts": accounts}
    return json.dumps(response, indent=4, default=json_default, ensure_ascii=False)


# 输入订单ID查询订单所有信息
@app.route('/queryAccount/<account_id>', methods=['GET'])
def query_specify_account(account_id):
    specify_account = query_specific_account_info(account_id)
    json_data = json.dumps(specify_account, indent=4, default=json_default, ensure_ascii=False)
    return json_data


# 新增订单
@app.route('/addAccount', methods=['POST'])
def add_account():
    content = request.form.get('content')
    number = request.form.get('number')
    person = request.form.get('person')
    create_time = request.form.get('createTime')

    account = Account(account_id=0, content=content, number=number, create_time=create_time, person=person)

    if add_new_account(account):
        return jsonify({'statue': 'successful'})
    else:
        return jsonify({'statue': 'failure'})


@app.route('/deleteAccount/<account_id>', methods=['GET'])
def delete_by_account_id(account_id):
    if delete_account_by_id(account_id):
        return jsonify({'statue': 'successful'})
    else:
        return jsonify({'statue': 'failure'})


@app.route('/updateAccount', methods=['GET'])
def change_account():
    a_id = request.form.get('account_id')
    content = request.form.get('content')
    number = request.form.get('number')
    person = request.form.get('person')
    create_time = request.form.get('createTime')

    account = Account(account_id=a_id, content=content, number=number, create_time=create_time, person=person)

    if update_account_info(account):
        return jsonify({'statue': 'successful'})
    else:
        return jsonify({'statue': 'failure'})


if __name__ == "__main__":
    hostname = socket.gethostname()
    ip = socket.gethostbyname(hostname)
    app.run(host=ip, port=5000, debug=False)
