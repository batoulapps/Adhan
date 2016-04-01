from setuptools import setup


setup(
    version='1.0.0',
    name='adhan',
    description='Islamic Prayer Times Calculations',
    long_description='Some longer description',
    classifiers=[
        'Development Status :: 3 - Alpha',
        'License :: OSI Approved :: MIT License',
        'Programming Language :: Python :: 3.5',
        'Topic :: Religion',
        'Topic :: Scientific/Engineering :: Astronomy'
    ],
    keywords='adhan prayer time fajr sunrise dhuhr asr maghrib sunset isha muslim islam',
    url='http://github.com/batoulapps/adhan',
    author='Matthew Crenshaw',
    author_email='matthew.crenshaw@batoulapps.com',
    license='MIT',
    packages=['adhan'],
    include_package_data=True,
    zip_safe=False,
    test_suite='nose.collector',
    tests_require=['nose']
)
