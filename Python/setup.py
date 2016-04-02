from setuptools import setup


setup(
    version='0.1.0',
    name='adhan-batoulapps',
    description='Islamic Prayer Times Calculations',
    long_description='Port of Batoul Apps\' Swift Adhan library',
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Natural Language :: English',
        'License :: OSI Approved :: MIT License',
        'Operating System :: OS Independent',
        'Programming Language :: Python :: 3.5',
        'Topic :: Religion',
        'Topic :: Scientific/Engineering :: Astronomy'
    ],
    keywords='adhan prayer time fajr sunrise dhuhr asr maghrib sunset isha muslim islam salah',
    url='http://github.com/batoulapps/adhan',
    author='Matthew Crenshaw',
    author_email='matthew.crenshaw@batoulapps.com',
    license='MIT',
    packages=['adhan-batoulapps'],
    include_package_data=False,
    zip_safe=False,
    test_suite='nose.collector',
    tests_require=['nose']
)
