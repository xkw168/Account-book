#!usr/bin/env python3
# -*- coding: utf-8 -*-
import json
import sqlite3  # 用于操作后端数据库

from account import Account


def json_default(obj):
    return obj.__dict__


def connect_sql():
    """
    establish connection
    :return:
    """
    connection = sqlite3.connect("Account.db")
    cursor = connection.cursor()
    return connection, cursor


def close_connection(connection, cursor):
    """
    close database connection
    :param connection:
    :param cursor:
    :return:
    """
    cursor.close()
    connection.close()


def query_all_account_info(offset, limit):
    """
    query all info of accounts
    :param offset: offset
    :param limit: limit
    :return: accounts
    """
    connection, cursor = connect_sql()
    sql = "select id, createTime, content, number, person from account " \
          "order by id desc " \
          "LIMIT %s OFFSET %s" % (limit, offset)
    cursor.execute(sql)
    result_account = cursor.fetchall()
    close_connection(connection, cursor)

    accounts = []
    for row_account in result_account:
        account = Account(account_id=row_account[0], create_time=row_account[1], content=row_account[2],
                          number=row_account[3], person=row_account[4])
        accounts.append(account)
    return accounts


def query_specific_account_info(account_id):
    """
    query all info of one account based on the account id
    :param account_id: specify account id
    :return: account entity
    """
    connection, cursor = connect_sql()
    sql = "select * from account where id = ?"
    cursor.execute(sql, (account_id,))
    result_account = cursor.fetchall()

    close_connection(connection, cursor)

    row_account = result_account[0]

    account = Account(account_id=row_account[0], create_time=row_account[1], content=row_account[2],
                      number=row_account[3], person=row_account[4])

    return account


def add_new_account(new_account):
    """
    add new account
    :param new_account: new account entity
    :return: True if successful
    """
    connection, cursor = connect_sql()
    sql = ''' insert into account
                              (content, number, createTime, person)
                              values
                              (?, ?, ?, ?)'''
    cursor.execute(sql, (new_account.content, new_account.number, new_account.create_time, new_account.person))
    id = cursor.lastrowid
    connection.commit()
    close_connection(connection, cursor)
    return True


def delete_account_by_id(account_id):
    """
    delete specific account based on its id
    :param account_id:
    :return: True if successful
    """
    connection, cursor = connect_sql()
    sql = '''delete from account where id = ?'''
    cursor.execute(sql, (account_id,))
    connection.commit()
    close_connection(connection, cursor)
    return True


def update_account_info(account):
    """
    update specific account info based on account id
    :param account:
    :return: True if successful
    """
    connection, cursor = connect_sql()
    sql = '''update account set (content, number, createTime, person)
                              values
                              (?, ?, ?, ?) where id = ?'''
    cursor.execute(sql, (account.content, account.number, account.create_time, account.person, account.account_id))
    connection.commit()
    close_connection(connection, cursor)
    return True


if __name__ == "__main__":
    print("hello")
