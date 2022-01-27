from jsonrpcclient import Ok, request, parse
import asyncio
import logging
from aiohttp import ClientSession


async def main():
    async with ClientSession() as session:
        async with session.post('https://rpc.testnet.near.org',
                                json=request("query",
                                             id="dontcare",
                                             params={
                                                 "request_type":
                                                 "view_account",
                                                 "finality": "final",
                                                 "account_id": "ycli.testnet"
                                             })) as response:
            parsed = parse(await response.json())
            if isinstance(parsed, Ok):
                print(parsed.result)
            else:
                logging.error(parsed.message)


asyncio.get_event_loop().run_until_complete(main())
