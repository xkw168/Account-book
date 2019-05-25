import json

import requests

from account import Account
from openpyxl import Workbook


def generate_account_header():
    return ["时间", "金额", "付款人", "备注"]


def account_2_str_list(account):
    account_str = [account.create_time, account.number, account.person,
                 account.content]
    return account_str


def generate_excel(accounts):
    # new workbook
    wb = Workbook()

    # grab the active worksheet(default the first one)
    ws1 = wb.active
    ws1.title = "账单"

    # generate the header
    ws1.append(generate_account_header())

    # start from 1, and header occupy 1 row
    index = 2
    for account in accounts:
        ws1.append(account_2_str_list(account))
        index += 1

    # Save the file
    wb.save("./账单.xlsx")


def query_all_account():
    def deserialization(json_str):
        if "_content" not in json_str.keys():
            return json_str
        else:
            return Account(
                content=json_str["_content"],
                create_time=json_str["_create_time"],
                number=json_str["_number"],
                person=json_str["_person"]
            )

    url = "your_url_here/queryAllAccount/%s/%s" % (0, 100)
    r = requests.get(url=url)
    # print(r.text)
    accounts = json.loads(r.text, object_hook=deserialization)
    return accounts["accounts"]


if __name__ == "__main__":
    accounts = query_all_account()
    accounts.reverse()

    result = {}

    sum_number = 0
    cnt = 1

    generate_excel(accounts)

    for account in accounts:
        if account.person in result.keys():
            result[account.person] += account.number
        else:
            result[account.person] = account.number

        sum_number += account.number
        print("账单%s" % cnt)
        print("日期：", account.create_time)
        print("款项：", account.content)
        print("金额：", account.number)
        print("付款人：", account.person)
        print()
        cnt += 1

    average = sum_number / len(result.keys())

    print("账单总结")
    print("总计:", sum_number)
    print("人均:", average)
    print()

    for person in result.keys():
        print("%s:" % person, result[person], "    应付：%.2f" % (result[person] - average))

