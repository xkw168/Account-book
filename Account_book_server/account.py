#!usr/bin/env python3


class Account:
    def __init__(self, account_id=0, content="", number=0,
                 person="", create_time=""):
        self._account_id = account_id
        self._content = content
        self._number = number
        self._person = person
        self._create_time = create_time

    @property
    def account_id(self):
        return self._account_id

    @property
    def content(self):
        return self._content

    @property
    def number(self):
        return self._number

    @property
    def create_time(self):
        return self._create_time

    @property
    def person(self):
        return self._person
